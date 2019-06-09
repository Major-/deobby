package rs.eumulate.deobby.asm.wrapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.objectweb.asm.tree.*

object Assertions {

    fun assertInstructionEquals(expected: InsnList, actual: InsnList) {
        assertInstructionEquals(expected.toArray(), actual)
    }

    fun assertInstructionEquals(expected: Array<AbstractInsnNode>, actual: InsnList) {
        assertInstructionEquals(expected, actual.toArray())
    }

    fun assertInstructionEquals(expected: Array<AbstractInsnNode>, actual: Array<AbstractInsnNode>) {
        assertEquals(expected.size, actual.size) { "Array size mismatch" }

        for (index in expected.indices) {
            val left = expected[index]
            val right = actual[index]

            assertInstructionEquals(left, right) { "index=$index" }
        }
    }

    fun assertInstructionEquals(expected: AbstractInsnNode, actual: AbstractInsnNode, message: () -> String = { "" }) {
        assertEquals(expected.opcode, actual.opcode) { "Opcode mismatch ${message()}" }

        when (expected) {
            is FieldInsnNode -> check(expected, actual, { owner }, { name }, { desc })
            is FrameNode -> check(expected, actual, { type }, { local }, { stack })
            is InsnNode -> return
            is IincInsnNode -> check(expected, actual, { `var` }, { incr })
            is IntInsnNode -> check(expected, actual, { operand })
            is InvokeDynamicInsnNode -> throw UnsupportedOperationException("InvokeDynamicInsnNode not supported")
            is JumpInsnNode -> check(expected, actual, { label })
            is LabelNode -> check(expected, actual, { label })
            is LdcInsnNode -> check(expected, actual, { cst })
            is LineNumberNode -> {
                assertEquals(expected.line, (actual as LineNumberNode).line)
            }
            is LookupSwitchInsnNode -> throw UnsupportedOperationException("LookupSwitchInsnNode not supported")
            is MethodInsnNode -> check(expected, actual, { owner }, { name }, { desc }, { itf })
            is MultiANewArrayInsnNode -> check(expected, actual, { desc }, { dims })
            is TableSwitchInsnNode -> throw UnsupportedOperationException("TableSwitchInsnNode not supported")
            is TypeInsnNode -> check(expected, actual, { desc })
            is VarInsnNode -> check(expected, actual, { `var` })
        }
    }

    private inline fun <reified T : AbstractInsnNode> check(
        first: T,
        second: AbstractInsnNode,
        vararg values: T.() -> Any?
    ) {
        val casted = requireNotNull(second as? T) {
            "Expected node of type `${T::class.java.name}`, was `${second::class.java.name}`."
        }

        values.forEach { assertEquals(first.it(), casted.it()) }
    }

}