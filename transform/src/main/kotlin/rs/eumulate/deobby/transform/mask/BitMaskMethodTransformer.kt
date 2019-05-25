package rs.eumulate.deobby.transform.mask

import mu.KotlinLogging
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodNode
import rs.eumulate.deobby.asm.InstructionMatcher
import rs.eumulate.deobby.asm.InstructionPattern
import rs.eumulate.deobby.asm.getNumericPushValue
import rs.eumulate.deobby.asm.toPushInstruction
import rs.eumulate.deobby.transform.PureMethodTransformer

/**
 * A [PureMethodTransformer] that simplifies bitmask operations of the form `a OP b SHIFT c`, where `OP` is one of `&`,
 * `|`, `^`, and `SHIFT` is one of `<<`, `>>`, `>>>`.
 *
 * Operations may be simplified when the number of bits in the mask is greater than the amount of bits remaining after
 * the shift has been applied.
 */
class BitMaskMethodTransformer : PureMethodTransformer {

    override fun transform(item: MethodNode) {
        val matcher = InstructionMatcher(item.instructions)
        val matches = matcher.match(BIT_MASK_SHIFT_PATTERN)

        for (match in matches) {
            val (pushMask, _, pushShift, shift) = match

            val mask = pushMask.getNumericPushValue()
            val bits = pushShift.getNumericPushValue().toInt()

            val simpleMask = if (shift.opcode in RIGHT_SHIFT_OPCODES) {
                mask ushr bits shl bits
            } else {
                mask shl bits ushr bits
            }

            item.instructions[pushMask] = if (shift.opcode in LONG_SHIFT_OPCODES) {
                LdcInsnNode(simpleMask)
            } else {
                simpleMask.toPushInstruction()
            }
        }

        logger.debug { "Rewrote ${matches.size} bitmasks in ${item.name}${item.signature}" }
    }

    companion object {
        private val logger = KotlinLogging.logger { }

        private const val PUSH_NUMBER = "(ICONST | BIPUSH | SIPUSH | LDC)"
        private const val BIT_OP = "(IAND | IOR | IXOR | LAND | LOR | LXOR)"
        private const val SHIFT = "(ISHL | ISHR | IUSHR | LSHL | LSHR | LUSHR)"

        private val BIT_MASK_SHIFT_PATTERN = InstructionPattern.compile("$PUSH_NUMBER $BIT_OP $PUSH_NUMBER $SHIFT")

        private val LONG_SHIFT_OPCODES = setOf(Opcodes.LSHL, Opcodes.LSHR, Opcodes.LUSHR)
        private val RIGHT_SHIFT_OPCODES = setOf(Opcodes.ISHR, Opcodes.IUSHR, Opcodes.LSHR, Opcodes.LUSHR)
    }

}
