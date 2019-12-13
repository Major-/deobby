package rs.emulate.deobby.transform.mask

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
import rs.eumulate.deobby.transform.mask.VerboseBitMaskMethodTransformer

class VerboseBitMaskMethodTransformerTest : PureMethodTransformerTest() {

    override val transformer = VerboseBitMaskMethodTransformer()

    @Test
    fun `transformer doesn't match bitmasks without shifts`() {
        val expected = arrayOf(0.toPushInstruction(), 1.toPushInstruction(), InsnNode(Opcodes.IAND))
        val input = expected.copyOf()

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer doesn't match bitshifts without masks`() {
        val expected = arrayOf(0.toPushInstruction(), 1.toPushInstruction(), InsnNode(Opcodes.LSHL))
        val input = expected.copyOf()

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer ignores sufficiently small mask values`() {
        val expected = expression(0.toPushInstruction(), "&", 1.toPushInstruction(), "<<", 1.toPushInstruction())
        val input = expected.copyOf()

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer matches the AND mask operation`() {
        val expected = expression(0.toPushInstruction(), "&", 0b1111.toPushInstruction(), "<<", 28.toPushInstruction())
        val input = expression(0.toPushInstruction(), "&", 0b1111111.toPushInstruction(), "<<", 28.toPushInstruction())

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer matches the OR mask operation`() {
        val expected = expression(0.toPushInstruction(), "|", 0b1111.toPushInstruction(), "<<", 28.toPushInstruction())
        val input = expression(0.toPushInstruction(), "|", 0b1111111.toPushInstruction(), "<<", 28.toPushInstruction())

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer matches the XOR mask operation`() {
        val expected = expression(0.toPushInstruction(), "^", 0b1111.toPushInstruction(), "<<", 28.toPushInstruction())

        val input = expression(0.toPushInstruction(), "^", 0b1111111.toPushInstruction(), "<<", 28.toPushInstruction())

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in left int shift`() {
        val expected = expression(0.toPushInstruction(), "&", 0b1111.toPushInstruction(), "<<", 28.toPushInstruction())
        val input = expression(0.toPushInstruction(), "&", 0b1111111.toPushInstruction(), "<<", 28.toPushInstruction())

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in left long shift`() {
        val expected = expression(LongLdcInsnNode(0), "&", LongLdcInsnNode(0b1111), "<<", 60.toPushInstruction())
        val input = expression(LongLdcInsnNode(0), "&", LdcInsnNode(Long.MAX_VALUE), "<<", 60.toPushInstruction())

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in right int shift`() {
        val expected = expression(0.toPushInstruction(), "&", 0b10000.toPushInstruction(), ">>", 4.toPushInstruction())
        val input = expression(0.toPushInstruction(), "&", 0b11011.toPushInstruction(), ">>", 4.toPushInstruction())

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in right long shift`() {
        val expected = expression(LongLdcInsnNode(0), "&", LongLdcInsnNode(0b1000), ">>", 3.toPushInstruction())
        val input = expression(LongLdcInsnNode(0), "&", LongLdcInsnNode(0b1011), ">>", 3.toPushInstruction())

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in unsigned right int shift`() {
        val expected = expression(0.toPushInstruction(), "&", 0b10000.toPushInstruction(), ">>>", 4.toPushInstruction())
        val input = expression(0.toPushInstruction(), "&", 0b11011.toPushInstruction(), ">>>", 4.toPushInstruction())

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer removes unnecessary bits in unsigned right long shift`() {
        val expected = expression(LongLdcInsnNode(0), "&", LongLdcInsnNode(1L shl 62), ">>>", 62.toPushInstruction())
        val input = expression(LongLdcInsnNode(0), "&", LongLdcInsnNode(Long.MAX_VALUE), ">>>", 62.toPushInstruction())

        assertInstructionEquals(expected, input)
    }

    @Test
    fun `transformer rewrites multiple bitmasks in one method`() {
        val expected = expression(0.toPushInstruction(), "&", 0b111.toPushInstruction(), "<<", 29.toPushInstruction()) +
            expression(LongLdcInsnNode(0), "&", LongLdcInsnNode(1L shl 62), ">>>", 62.toPushInstruction())

        val input = expression(0.toPushInstruction(), "&", 0b1111.toPushInstruction(), "<<", 29.toPushInstruction()) +
            expression(LongLdcInsnNode(0), "&", LongLdcInsnNode(Long.MAX_VALUE), ">>>", 62.toPushInstruction())

        assertInstructionEquals(expected, input)
    }

    private companion object {

        /**
         * Creates a bitmask expression.
         *
         * @param shift Must be [InsnNode], [IntInsnNode], or an [LdcInsnNode] with an [Int] value.
         */
        private fun expression(
            value: AbstractInsnNode,
            maskop: String,
            mask: AbstractInsnNode,
            shiftop: String,
            shift: AbstractInsnNode
        ): Array<AbstractInsnNode> {
            require(!shift.isLongValue()) { "Amount to shift by cannot be a long." }
            require(value.isLongValue() == mask.isLongValue()) {
                "Cannot mix value and mask types - either both must be longs, or neither."
            }

            val maskOp = when (maskop) {
                "&" -> if (value.isLongValue() && mask.isLongValue()) Opcodes.LAND else Opcodes.IAND
                "|" -> if (value.isLongValue() && mask.isLongValue()) Opcodes.LOR else Opcodes.IOR
                "^" -> if (value.isLongValue() && mask.isLongValue()) Opcodes.LXOR else Opcodes.IXOR
                else -> error("Unrecognised mask operation $maskop")
            }

            val shiftOp = when (shiftop) {
                "<<" -> if (value.isLongValue() && mask.isLongValue()) Opcodes.LSHL else Opcodes.ISHL
                ">>" -> if (value.isLongValue() && mask.isLongValue()) Opcodes.LSHR else Opcodes.ISHR
                ">>>" -> if (value.isLongValue() && mask.isLongValue()) Opcodes.LUSHR else Opcodes.IUSHR
                else -> error("Unrecognised shift operation $shiftop")
            }

            return arrayOf(value, mask, InsnNode(maskOp), shift, InsnNode(shiftOp))
        }

        private fun AbstractInsnNode.isLongValue(): Boolean {
            return this is LdcInsnNode && isLong()
        }

    }

}
