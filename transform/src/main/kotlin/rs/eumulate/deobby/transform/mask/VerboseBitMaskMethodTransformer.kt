package rs.eumulate.deobby.transform.mask

import com.github.michaelbull.logging.InlineLogger
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode
import rs.eumulate.deobby.asm.ldc.LongLdcInsnNode
import rs.eumulate.deobby.asm.match.InstructionPattern
import rs.eumulate.deobby.asm.toPushInstruction
import rs.eumulate.deobby.asm.tree.getNumericPushValue
import rs.eumulate.deobby.asm.tree.match
import rs.eumulate.deobby.asm.tree.printableName
import rs.eumulate.deobby.transform.MethodContext
import rs.eumulate.deobby.transform.PureMethodTransformer

/**
 * A [PureMethodTransformer] that compacts bitmask operands in expressions of the form `a OP b SHIFT c`, where `OP` is
 * one of `&`, `|`, `^`, and `SHIFT` is one of `<<`, `>>`, `>>>`.
 *
 * Operations may be compacted when the number of bits in the mask operand is greater than the amount of bits remaining
 * after the shift has been applied.
 */
class VerboseBitMaskMethodTransformer : PureMethodTransformer() {

    override fun transform(item: MethodNode, context: MethodContext) {
        val matches = item.match(BIT_MASK_SHIFT_PATTERN)

        for ((pushMask, /* bit op */ _, pushShift, shift) in matches) {
            val mask = pushMask.getNumericPushValue()
            val bits = pushShift.getNumericPushValue().toInt()

            item.instructions[pushMask] = if (shift.opcode in LONG_SHIFT_OPCODES) {
                val simpleMask = if (shift.opcode in RIGHT_SHIFT_OPCODES) {
                    mask ushr bits shl bits
                } else {
                    mask shl bits ushr bits
                }

                logger.trace {
                    "Simplifying long shift of $mask to $simpleMask in ${context.className}/${item.printableName}"
                }

                LongLdcInsnNode(simpleMask) // Must use LDC with a Long regardless of the mask value if long shift
            } else {
                val truncated = mask.toInt()
                val simpleMask = if (shift.opcode in RIGHT_SHIFT_OPCODES) {
                    truncated ushr bits shl bits
                } else {
                    truncated shl bits ushr bits
                }

                logger.trace {
                    "Simplifying int shift of $truncated to $simpleMask in ${context.className}.${item.printableName}"
                }

                simpleMask.toPushInstruction()
            }
        }

        if (matches.isNotEmpty()) {
            logger.debug { "Simplified ${matches.size} bitmasks in ${context.className}.${item.printableName}" }
        }
    }

    private companion object {
        private val logger = InlineLogger()

        private const val PUSH_NUMBER = "(ICONST | BIPUSH | SIPUSH | LDC)"
        private const val BIT_OP = "(IAND | IOR | IXOR | LAND | LOR | LXOR)"
        private const val SHIFT_OP = "(ISHL | ISHR | IUSHR | LSHL | LSHR | LUSHR)"

        private val BIT_MASK_SHIFT_PATTERN = InstructionPattern.compile("$PUSH_NUMBER $BIT_OP $PUSH_NUMBER $SHIFT_OP")

        private val LONG_SHIFT_OPCODES = hashSetOf(Opcodes.LSHL, Opcodes.LSHR, Opcodes.LUSHR)
        private val RIGHT_SHIFT_OPCODES = hashSetOf(Opcodes.ISHR, Opcodes.IUSHR, Opcodes.LSHR, Opcodes.LUSHR)
    }

}
