package rs.emulate.deobby.transform.shift

import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import rs.emulate.deobby.transform.PureMethodTransformerTest
import rs.eumulate.deobby.asm.ldc.LongLdcInsnNode
import rs.eumulate.deobby.asm.ldc.isLong
import rs.eumulate.deobby.asm.toPushInstruction
import rs.eumulate.deobby.transform.shift.VerboseBitShiftMethodTransformer

class VerboseBitShiftMethodTransformerTest : PureMethodTransformerTest() {

    override val transformer = VerboseBitShiftMethodTransformer()

    @Test
    fun `transformer ignores appropriately-sized int shift value`() {
        val expected = expression(0.toPushInstruction(), 31.toPushInstruction(), "<<")
        val input = expected.copyOf()

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer ignores appropriately-sized long shift value`() {
        val expected = expression(LongLdcInsnNode(0), 63.toPushInstruction(), "<<")
        val input = expected.copyOf()

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in left int shift`() {
        val expected = expression(0.toPushInstruction(), 0.toPushInstruction(), "<<")
        val input = expression(0.toPushInstruction(), 32.toPushInstruction(), "<<")

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in left long shift`() {
        val expected = expression(LongLdcInsnNode(0), 1.toPushInstruction(), "<<")
        val input = expression(LongLdcInsnNode(0), 65.toPushInstruction(), "<<")

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in right int shift`() {
        val expected = expression(0.toPushInstruction(), 1.toPushInstruction(), ">>")
        val input = expression(0.toPushInstruction(), 33.toPushInstruction(), ">>")

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in right long shift`() {
        val expected = expression(LongLdcInsnNode(0), 1.toPushInstruction(), ">>")
        val input = expression(LongLdcInsnNode(0), 65.toPushInstruction(), ">>")

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in unsigned right int shift`() {
        val expected = expression(0.toPushInstruction(), 1.toPushInstruction(), ">>>")
        val input = expression(0.toPushInstruction(), 33.toPushInstruction(), ">>>")

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in unsigned right long shift`() {
        val expected = expression(LongLdcInsnNode(0), 1.toPushInstruction(), ">>>")
        val input = expression(LongLdcInsnNode(0), 65.toPushInstruction(), ">>>")

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer rewrites multiple bitmasks in one method`() {
        val expected = expression(0.toPushInstruction(), 5.toPushInstruction(), "<<") +
            expression(LongLdcInsnNode(0), 5.toPushInstruction(), "<<")

        val input = expression(0.toPushInstruction(), 37.toPushInstruction(), "<<") +
            expression(LongLdcInsnNode(0), 69.toPushInstruction(), "<<")

        assertInstructionEquals(expected, input)
    }

    private companion object {

        /**
         * Creates a bitshift expression.
         *
         * @param shift Must be an [InsnNode], [IntInsnNode], or an [LdcInsnNode] with an [Int] value.
         */
        private fun expression(
            value: AbstractInsnNode,
            shift: AbstractInsnNode,
            shiftop: String
        ): Array<AbstractInsnNode> {
            require(!shift.isLongValue()) { "Amount to shift by cannot be a long." }

            val shiftOp = when (shiftop) {
                "<<" -> if (value.isLongValue()) Opcodes.LSHL else Opcodes.ISHL
                ">>" -> if (value.isLongValue()) Opcodes.LSHR else Opcodes.ISHR
                ">>>" -> if (value.isLongValue()) Opcodes.LUSHR else Opcodes.IUSHR
                else -> error("Unrecognised shift operation $shiftop")
            }

            return arrayOf(value, shift, InsnNode(shiftOp))
        }

        private fun AbstractInsnNode.isLongValue(): Boolean {
            return this is LdcInsnNode && this.isLong()
        }

    }

}
