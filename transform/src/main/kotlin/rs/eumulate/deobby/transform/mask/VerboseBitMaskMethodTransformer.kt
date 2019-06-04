package rs.eumulate.deobby.transform.mask

import mu.KotlinLogging
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode
import rs.eumulate.deobby.asm.InstructionMatcher
import rs.eumulate.deobby.asm.InstructionPattern
import rs.eumulate.deobby.asm.getNumericPushValue
import rs.eumulate.deobby.asm.ldc.LongLdcInsnNode
import rs.eumulate.deobby.asm.toPushInstruction
import rs.eumulate.deobby.asm.tree.printableName
import rs.eumulate.deobby.transform.PureMethodTransformer

/**
 * A [PureMethodTransformer] that compacts bitmask operands in expression of the form `a OP b SHIFT c`, where `OP` is
 * one of `&`, `|`, `^`, and `SHIFT` is one of `<<`, `>>`, `>>>`.
 *
 * Operations may be compacted when the number of bits in the mask operand is greater than the amount of bits remaining
 * after the shift has been applied.
 */
class VerboseBitMaskMethodTransformer : PureMethodTransformer {

    override fun transform(item: MethodNode) {
        val matcher = InstructionMatcher(item.instructions)
        val matches = matcher.match(BIT_MASK_SHIFT_PATTERN)

        for (match in matches) {
            val (pushMask, _, pushShift, shift) = match

            val mask: Long = pushMask.getNumericPushValue()
            val bits = pushShift.getNumericPushValue().toInt()

            item.instructions[pushMask] = if (shift.opcode in LONG_SHIFT_OPCODES) {
                val simpleMask = if (shift.opcode in RIGHT_SHIFT_OPCODES) {
                    mask ushr bits shl bits
                } else {
                    mask shl bits ushr bits
                }

                logger.debug { "Simplifying long shift of $mask to $simpleMask in ${item.printableName}" }
                LongLdcInsnNode(simpleMask) // Must use a LDC with a Long regardless of the mask value if long shift
            } else {
                val truncated = mask.toInt()
                val simpleMask = if (shift.opcode in RIGHT_SHIFT_OPCODES) {
                    truncated ushr bits shl bits
                } else {
                    truncated shl bits ushr bits
                }

                logger.debug { "Simplifying int shift of $truncated to $simpleMask in ${item.printableName}" }
                simpleMask.toPushInstruction()
            }
        }

        if (matches.isNotEmpty()) {
            logger.info { "Simplified ${matches.size} bitmasks in ${item.printableName}" }
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger { }

        private const val PUSH_NUMBER = "(ICONST | BIPUSH | SIPUSH | LDC)"
        private const val BIT_OP = "(IAND | IOR | IXOR | LAND | LOR | LXOR)"
        private const val SHIFT_OP = "(ISHL | ISHR | IUSHR | LSHL | LSHR | LUSHR)"

        private val BIT_MASK_SHIFT_PATTERN = InstructionPattern.compile("$PUSH_NUMBER $BIT_OP $PUSH_NUMBER $SHIFT_OP")

        private val LONG_SHIFT_OPCODES = setOf(Opcodes.LSHL, Opcodes.LSHR, Opcodes.LUSHR)
        private val RIGHT_SHIFT_OPCODES = setOf(Opcodes.ISHR, Opcodes.IUSHR, Opcodes.LSHR, Opcodes.LUSHR)
    }

}
