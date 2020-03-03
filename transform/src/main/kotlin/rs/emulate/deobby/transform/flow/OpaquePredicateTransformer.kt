package rs.emulate.deobby.transform.flow

import com.github.michaelbull.logging.InlineLogger
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import rs.emulate.deobby.asm.match.InstructionMatch
import rs.emulate.deobby.asm.match.InstructionMatchConstraint
import rs.emulate.deobby.asm.match.InstructionMatcher
import rs.emulate.deobby.asm.match.InstructionPattern
import rs.emulate.deobby.asm.tree.hasBytecode
import rs.emulate.deobby.asm.tree.printableName
import rs.emulate.deobby.asm.tree.remove
import rs.emulate.deobby.asm.wrapper.FieldReference
import rs.emulate.deobby.asm.wrapper.MethodReference
import rs.emulate.deobby.asm.wrapper.asReference
import rs.emulate.deobby.asm.wrapper.referencedBy
import rs.emulate.deobby.transform.MethodContext
import rs.emulate.deobby.transform.MethodTransformer
import rs.emulate.deobby.transform.Program

class OpaquePredicateTransformer : MethodTransformer {

    private lateinit var obstructors: Set<FieldReference>

    private lateinit var initializers: Map<MethodReference, List<InstructionMatch>>

    override fun initialise(program: Program) {
        val obstructors = mutableSetOf<FieldReference>()
        val initialisers = mutableMapOf<MethodReference, List<InstructionMatch>>()

        for (clazz in program.classes()) {
            for (method in clazz.methods) {
                if (!method.hasBytecode()) {
                    continue
                }

                val matcher = InstructionMatcher(method.instructions)
                val matches = matcher.match(FLOW_OBSTRUCTOR_INITIALIZER_PATTERN)

                for (match in matches) {
                    val put = match.last() as FieldInsnNode
                    obstructors += put.asReference()
                }

                initialisers[MethodReference(clazz.name, method.name, method.desc)] = matches

                logger.trace { "Identified $matches flow obstructors in ${clazz.name}.${method.printableName}." }
            }
        }

        logger.debug { "Initialised OpaquePredicateTransformer, identified ${obstructors.size} obstructors." }

        this.initializers = initialisers
        this.obstructors = obstructors
    }

    override fun transform(item: MethodNode, context: MethodContext) {
        val initializers = initializers[MethodReference(context.className, item.name, item.desc)] ?: return
        for (match in initializers) {
            match.drop(2).forEach(item.instructions::remove)
        }

        val instructions = item.instructions
        val matcher = InstructionMatcher(instructions)
        val constraint = OpaquePredicateConstraint(obstructors, matcher)

        val predicates = matcher.match(OPAQUE_PREDICATE_PATTERN, constraint)
        for (match in predicates) {
            val (load, branch) = match; branch as JumpInsnNode

            when (val opcode = branch.opcode) {
                Opcodes.IFEQ -> {
                    instructions.remove(load)
                    branch.opcode = Opcodes.GOTO
                }
                Opcodes.IFNE -> instructions.remove(load, branch)
                else -> throw IllegalStateException("Unknown opcode: $opcode")
            }
        }

        val stores = matcher.match(STORE_PATTERN)
        for (storeMatch in stores) {
            val (get, store) = storeMatch; get as FieldInsnNode

            if (get.asReference() in obstructors) {
                instructions.remove(get, store)
            }
        }

        if (predicates.isNotEmpty()) {
            logger.debug { "Removed ${predicates.size} opaque predicates from ${context.printableName(item)}" }
        }
    }

    override fun finish(program: Program) {
        for (put in obstructors) {
            val owner = program[put.owner]
            owner.fields.removeIf { it.referencedBy(put) }
        }
    }

    private inner class OpaquePredicateConstraint(
        private val obstructors: Set<FieldReference>,
        private val matcher: InstructionMatcher
    ) : InstructionMatchConstraint {

        override fun invoke(match: InstructionMatch): Boolean {
            val load = match.first()

            /* check if a flow obstructor is loaded directly */
            if (load.opcode == Opcodes.GETSTATIC) {
                val get = load as FieldInsnNode
                return get.asReference() in obstructors
            }

            /* check if a flow obstructor is loaded via a local variable */
            val iload = load as VarInsnNode
            val stores = matcher.match(STORE_PATTERN)

            for (storeMatch in stores) {
                val (get, store) = storeMatch; get as FieldInsnNode; store as VarInsnNode

                if (iload.`var` == store.`var` && get.asReference() in obstructors) {
                    return true
                }
            }

            return false
        }

    }

    private companion object {
        private val logger = InlineLogger()

        private val FLOW_OBSTRUCTOR_INITIALIZER_PATTERN =
            InstructionPattern.compile("(GETSTATIC | ILOAD) IFEQ (((GETSTATIC ISTORE)? IINC ILOAD) | ((GETSTATIC | ILOAD) IFEQ ICONST GOTO ICONST)) PUTSTATIC")
        private val STORE_PATTERN = InstructionPattern.compile("GETSTATIC ISTORE")
        private val OPAQUE_PREDICATE_PATTERN = InstructionPattern.compile("(GETSTATIC | ILOAD) (IFEQ | IFNE)")
    }

}
