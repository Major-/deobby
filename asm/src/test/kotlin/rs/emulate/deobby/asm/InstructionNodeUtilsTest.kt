package rs.emulate.deobby.asm

import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import rs.emulate.deobby.asm.wrapper.Assertions.assertInstructionEquals

class InstructionNodeUtilsTest {

    @Test
    fun `Int toPushInstruction returns the correct instruction`() {
        assertInstructionEquals(InsnNode(ICONST_M1), (-1).toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_0), 0.toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_1), 1.toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_2), 2.toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_3), 3.toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_4), 4.toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_5), 5.toPushInstruction())

        assertInstructionEquals(IntInsnNode(BIPUSH, Byte.MIN_VALUE.toInt()), Byte.MIN_VALUE.toInt().toPushInstruction())
        assertInstructionEquals(IntInsnNode(BIPUSH, -2), (-2).toPushInstruction())
        assertInstructionEquals(IntInsnNode(BIPUSH, 6), 6.toPushInstruction())
        assertInstructionEquals(IntInsnNode(BIPUSH, Byte.MAX_VALUE.toInt()), Byte.MAX_VALUE.toInt().toPushInstruction())

        assertInstructionEquals(
            IntInsnNode(SIPUSH, Short.MIN_VALUE.toInt()),
            Short.MIN_VALUE.toInt().toPushInstruction()
        )
        assertInstructionEquals(IntInsnNode(SIPUSH, (Byte.MIN_VALUE - 1)), (Byte.MIN_VALUE - 1).toPushInstruction())
        assertInstructionEquals(IntInsnNode(SIPUSH, (Byte.MAX_VALUE + 1)), (Byte.MAX_VALUE + 1).toPushInstruction())
        assertInstructionEquals(
            IntInsnNode(SIPUSH, Short.MAX_VALUE.toInt()),
            Short.MAX_VALUE.toInt().toPushInstruction()
        )

        assertInstructionEquals(LdcInsnNode(Int.MIN_VALUE), Int.MIN_VALUE.toPushInstruction())
        assertInstructionEquals(LdcInsnNode(Short.MIN_VALUE - 1), (Short.MIN_VALUE - 1).toPushInstruction())
        assertInstructionEquals(LdcInsnNode(Short.MAX_VALUE + 1), (Short.MAX_VALUE + 1).toPushInstruction())
        assertInstructionEquals(LdcInsnNode(Int.MAX_VALUE), Int.MAX_VALUE.toPushInstruction())
    }

    @Test
    fun `Long toPushInstruction returns the correct instruction`() {
        assertInstructionEquals(InsnNode(ICONST_M1), (-1).toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_0), 0.toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_1), 1.toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_2), 2.toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_3), 3.toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_4), 4.toPushInstruction())
        assertInstructionEquals(InsnNode(ICONST_5), 5.toPushInstruction())

        assertInstructionEquals(IntInsnNode(BIPUSH, Byte.MIN_VALUE.toInt()), Byte.MIN_VALUE.toInt().toPushInstruction())
        assertInstructionEquals(IntInsnNode(BIPUSH, -2), (-2).toPushInstruction())
        assertInstructionEquals(IntInsnNode(BIPUSH, 6), 6.toPushInstruction())
        assertInstructionEquals(IntInsnNode(BIPUSH, Byte.MAX_VALUE.toInt()), Byte.MAX_VALUE.toInt().toPushInstruction())

        assertInstructionEquals(
            IntInsnNode(SIPUSH, Short.MIN_VALUE.toInt()),
            Short.MIN_VALUE.toInt().toPushInstruction()
        )
        assertInstructionEquals(IntInsnNode(SIPUSH, (Byte.MIN_VALUE - 1)), (Byte.MIN_VALUE - 1).toPushInstruction())
        assertInstructionEquals(IntInsnNode(SIPUSH, (Byte.MAX_VALUE + 1)), (Byte.MAX_VALUE + 1).toPushInstruction())
        assertInstructionEquals(
            IntInsnNode(SIPUSH, Short.MAX_VALUE.toInt()),
            Short.MAX_VALUE.toInt().toPushInstruction()
        )

        assertInstructionEquals(LdcInsnNode(Int.MIN_VALUE), Int.MIN_VALUE.toPushInstruction())
        assertInstructionEquals(LdcInsnNode(Short.MIN_VALUE - 1), (Short.MIN_VALUE - 1).toPushInstruction())
        assertInstructionEquals(LdcInsnNode(Short.MAX_VALUE + 1), (Short.MAX_VALUE + 1).toPushInstruction())
        assertInstructionEquals(LdcInsnNode(Int.MAX_VALUE), Int.MAX_VALUE.toPushInstruction())

        assertInstructionEquals(LdcInsnNode(Long.MIN_VALUE), Long.MIN_VALUE.toPushInstruction())
        assertInstructionEquals(LdcInsnNode(Int.MIN_VALUE - 1L), (Int.MIN_VALUE - 1L).toPushInstruction())
        assertInstructionEquals(LdcInsnNode(Int.MAX_VALUE + 1L), (Int.MAX_VALUE + 1L).toPushInstruction())
        assertInstructionEquals(LdcInsnNode(Long.MAX_VALUE), Long.MAX_VALUE.toPushInstruction())
    }

}
