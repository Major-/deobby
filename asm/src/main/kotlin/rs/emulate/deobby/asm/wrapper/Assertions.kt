package rs.emulate.deobby.asm.wrapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.objectweb.asm.tree.AbstractInsnNode

object Assertions {

    fun assertInstructionEquals(expected: Iterable<AbstractInsnNode>, actual: Iterable<AbstractInsnNode>) {
        val expectedList = expected.toList()
        val actualList = actual.toList()

        assertEquals(expectedList.size, actualList.size) { "Instruction count mismatch" }

        for (index in expectedList.indices) {
            val left = expectedList[index]
            val right = actualList[index]

            assertInstructionEquals(
                left,
                right
            ) { "index=$index" }
        }
    }

    fun assertInstructionEquals(expected: AbstractInsnNode, actual: AbstractInsnNode, message: () -> String = { "" }) {
        assertTrue(expected.equivalentTo(actual)) { "Instruction mismatch: " + message() }
    }

}