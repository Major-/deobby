package rs.emulate.deobby.asm.match

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.emulate.deobby.asm.match.InstructionPattern.Companion.compile
import rs.emulate.deobby.asm.match.InstructionPattern.Companion.instructionToString

class InstructionPatternTest {

    @Test
    fun `pattern cannot contain invalid instructions`() {
        val exception = assertThrows<IllegalArgumentException> {
            compile("invalidinstr")
        }

        assertEquals("invalidinstr is not a known instruction.", exception.message)
    }

    @Test
    fun `can compute a pattern containing only a single instruction`() {
        val expected = instructionToString("AALOAD")
        assertEquals(expected, compile("AALOAD").pattern.pattern())
    }

    @Test
    fun `can compute a pattern containing an OR`() {
        val expected = "${instructionToString("NOP")}|${instructionToString("AALOAD")}"
        assertEquals(expected, compile("NOP|AALOAD").pattern.pattern())
    }

    @Test
    fun `can compute a pattern containing a group`() {
        val expected = "(${instructionToString("LSHR")}|${instructionToString("DUP2")})"
        assertEquals(expected, compile("(LSHR|DUP2)").pattern.pattern())
    }

}