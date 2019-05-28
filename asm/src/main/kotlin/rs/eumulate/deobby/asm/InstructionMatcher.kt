package rs.eumulate.deobby.asm

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

class InstructionMatcher(list: InsnList) {

    private val instructions: List<AbstractInsnNode> = list.toArray().filter { it.opcode != -1 }

    private val instructionText by lazy { instructionsToString() }
    private val instructionTextReverse by lazy { instructionsToString(reverse = true) }

    fun match(
        pattern: InstructionPattern,
        reverse: Boolean = false,
        constraint: (InstructionPatternMatch) -> Boolean = { true }
    ): List<InstructionPatternMatch> {
        val matches = mutableListOf<InstructionPatternMatch>()

        val input = if (reverse) instructionTextReverse else instructionText
        val matcher = pattern.matcher(input)

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
    private fun instructionsToString(reverse: Boolean = false): String {
        val instructions = if (reverse) instructions.reversed() else instructions
        return instructions.joinToString(separator = "") { InstructionPattern.opcodeToString(it.opcode) }
    }

}
