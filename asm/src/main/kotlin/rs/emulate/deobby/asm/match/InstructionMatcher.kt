package rs.emulate.deobby.asm.match

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

class InstructionMatcher(list: InsnList) {

    private val instructions = list.toArray().filterNot { it.opcode == PSUEDO_INSTRUCTION_OPCODE }
    private val instructionText = instructionsToString()

    fun match(pattern: InstructionPattern): List<InstructionMatch> {
        val matches = mutableListOf<InstructionMatch>()
        val matcher = pattern.matcher(instructionText)

        while (matcher.find()) {
            matches += instructions.slice(matcher.start() until matcher.end())
        }

        return matches
    }

    fun match(pattern: InstructionPattern, constraint: InstructionMatchConstraint): List<InstructionMatch> {
        val matches = mutableListOf<InstructionMatch>()
        val matcher = pattern.matcher(instructionText)

        while (matcher.find()) {
            val match = instructions.slice(matcher.start() until matcher.end())

            if (constraint(match)) {
                matches += match
            }
        }

        return matches
    }

    /**
     * Converts the instruction list to the internal character format.
     */
    private fun instructionsToString(): String {
        return instructions.joinToString(separator = "") {
            InstructionPattern.opcodeToString(
                it.opcode
            )
        }
    }

    private companion object {

        /**
         * The opcode asm uses for pseudo-instructions, like labels
         */
        private const val PSUEDO_INSTRUCTION_OPCODE = -1

    }

}

/**
 * An individual match of an [InstructionPattern] produced by an [InstructionMatcher].
 */
typealias InstructionMatch = List<AbstractInsnNode>

/**
 * A constraint to filter [InstructionMatch]es.
 */
typealias InstructionMatchConstraint = (InstructionMatch) -> Boolean