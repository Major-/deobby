package rs.emulate.deobby.transform.dead

import com.github.michaelbull.logging.InlineLogger
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import rs.emulate.deobby.asm.match.InstructionMatcher
import rs.emulate.deobby.asm.match.InstructionPattern
import rs.emulate.deobby.asm.tree.isStatic
import rs.emulate.deobby.asm.wrapper.MethodReference
import rs.emulate.deobby.asm.wrapper.asReference
import rs.emulate.deobby.transform.ClassContext
import rs.emulate.deobby.transform.ClassTransformer
import rs.emulate.deobby.transform.Program

class DeadMethodRemoval : ClassTransformer {

    private val removals = mutableListOf<MethodReference>()

    override fun initialise(program: Program) {
        val calls = mutableMapOf<MethodReference, MutableList<MethodReference>>()

        for (clazz in program.classes()) {
            for (method in clazz.methods) {
                val matcher = InstructionMatcher(method.instructions)
                val matches = matcher.match(INVOKE_PATTERN)

                val caller = MethodReference(clazz.name, method.name, method.desc)
                calls.putIfAbsent(caller, ArrayList(2))

                for (match in matches) {
                    val invocation = match.first() as MethodInsnNode
                    val callee = invocation.asReference()

                    calls.getOrPut(callee) { mutableListOf() } += caller
                }
            }
        }

        for (item in program.classes()) {
            val parentMethods = findParentFunctions(program, item.name) ?: continue

            val iterator = item.methods.listIterator()
            for (method in iterator) {
                if (!method.isStatic() && Pair(method.name, method.desc) in parentMethods || retain(method)) {
                    continue
                }

                val current = MethodReference(item.name, method.name, method.desc)
                val currentCalls = checkNotNull(calls[current]) { "Found a function not in the call graph: $current" }

                // function can be unused if it's either never called, or only called by itself
                if (currentCalls.isEmpty() || currentCalls.size == 1 && currentCalls.first() == current) {
                    removals += current
                }
            }
        }
    }

    override fun transform(item: ClassNode, context: ClassContext) {
        var removed = 0
        val iterator = item.methods.listIterator()

        for (method in iterator) {
            val current = MethodReference(item.name, method.name, method.desc)

            if (current in removals) {
                logger.trace { "Removing unused function $current" }
                iterator.remove()
                removed++
            }
        }

        logger.debug { "Removed $removed unused methods (out of ${removed + item.methods.size}) from ${item.name}" }
    }

    /**
     * Returns whether or not the given [MethodNode] should be included, even if (or in spite of) it not being
     * directly referenced.
     */
    private fun retain(method: MethodNode): Boolean {
        val name = method.name

        if (method.isStatic() && name == "main" && method.desc == PSVM_DESCRIPTOR) {
            return true
        } else if (name == STATIC_INITIALIZER_NAME) {
            return true
        }

        return false
    }

    /**
     * Finds the names and descriptors of every method in every parent function, returning `null` if a supertype could
     * not be loaded (i.e. was not on the system classpath).
     */
    private fun findParentFunctions(program: Program, className: String): Set<Pair<String, String>>? {
        return findSupertypes(program, className).flatMapTo(mutableSetOf<Pair<String, String>>()) { type ->
            when (type) {
                is SuperType.JdkClass -> type.clazz.methods.map { it.name to Type.getMethodDescriptor(it) }
                is SuperType.Node -> type.node.methods.map { it.name to it.desc }
                is SuperType.NotAvailable -> return null
            }
        }
    }

    // TODO move this elsewhere (and share it between transformers)
    private fun findSupertypes(program: Program, className: String): Set<SuperType> {
        fun findSupertypes(
            program: Program,
            name: String,
            types: MutableSet<SuperType>,
            visited: MutableSet<String>
        ): Set<SuperType> {
            if (name in visited) {
                return types
            }

            visited += name

            if (name in program) {
                val node = program[name]
                val superName = node.superName

                types += SuperType.from(superName, program)
                findSupertypes(program, superName, types, visited)

                types += node.interfaces.map { SuperType.from(it, program) }
                for (interfaceName in node.interfaces) {
                    findSupertypes(program, interfaceName, types, visited)
                }
            } else {
                val clazz = load(name) ?: return types // bail out if we couldn't load the class

                val superclass = clazz.superclass ?: return types
                types += SuperType.JdkClass(superclass)
                types += clazz.interfaces.map(SuperType::JdkClass)

                findSupertypes(program, superclass.name.replace('.', '/'), types, visited)

                for (trait in clazz.interfaces) {
                    findSupertypes(program, trait.name.replace('.', '/'), types, visited)
                }
            }

            return types
        }

        return findSupertypes(program, className, mutableSetOf(), mutableSetOf())
    }

    private sealed class SuperType {
        data class Node(val node: ClassNode) : SuperType()
        data class JdkClass(val clazz: Class<*>) : SuperType()

        /**
         * The class with the specified [name] is not present on the classpath.
         */
        data class NotAvailable(val name: String) : SuperType()

        companion object {
            fun from(name: String, program: Program): SuperType {
                return if (name in program) {
                    Node(program[name])
                } else {
                    load(name)?.let(::JdkClass) ?: NotAvailable(name)
                }
            }
        }
    }

    private companion object {

        private val logger = InlineLogger()

        private val INVOKE_PATTERN = InstructionPattern.compile("MethodInsnNode")

        /**
         * The method descriptor for Java's `public static void main`.
         */
        private const val PSVM_DESCRIPTOR = "([Ljava/lang/String;)V"

        private const val STATIC_INITIALIZER_NAME = "<clinit>"

        private fun load(name: String): Class<*>? {
            val javaName = name.replace('/', '.')

            return try {
                ClassLoader.getSystemClassLoader().loadClass(javaName)
            } catch (e: ClassNotFoundException) {
                logger.warn(e) { "Referenced type $name could not be found on the system classpath." }
                null
            }
        }

    }

}