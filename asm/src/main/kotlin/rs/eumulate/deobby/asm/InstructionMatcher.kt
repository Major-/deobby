package rs.eumulate.deobby.asm

import org.objectweb.asm.tree.InsnList

class InstructionMatcher(list: InsnList) {

    private val instructions = list.toArray().filter { it.opcode != -1 }
    private val instructionText = instructionsToString()

    fun match(pattern: InstructionPattern): List<InstructionPatternMatch> {
        val matches = mutableListOf<InstructionPatternMatch>()
        val matcher = pattern.matcher(instructionText)

        while (matcher.find()) {
            matches += instructions.slice(matcher.start() until matcher.end())
        }

        return matches
    }

    fun match(
        pattern: InstructionPattern,
        constraint: (InstructionPatternMatch) -> Boolean
    ): List<InstructionPatternMatch> {
        val matches = mutableListOf<InstructionPatternMatch>()
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
        return instructions.joinToString(separator = "") { InstructionPattern.opcodeToString(it.opcode) }
    }

}
