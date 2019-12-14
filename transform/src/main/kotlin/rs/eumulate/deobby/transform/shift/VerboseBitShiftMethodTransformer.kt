package rs.eumulate.deobby.transform.shift

import mu.KotlinLogging
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode
import rs.eumulate.deobby.asm.match.InstructionPattern
import rs.eumulate.deobby.asm.toPushInstruction
import rs.eumulate.deobby.asm.tree.getNumericPushValue
import rs.eumulate.deobby.asm.tree.match
import rs.eumulate.deobby.transform.MethodContext
import rs.eumulate.deobby.transform.PureMethodTransformer

/**
 * A [PureMethodTransformer] that compacts bitshift operands by removing superfluous bits.
 *
 * The Java Language Specification require JVMs utilise only the lower 5 bits for an integer shift (6 bits for a long
 * shift), although the given operand may be any 32-bit value. This transformer strips the upper 27 (or 26) bits.
 *
 * ```
 * assertEquals(2 << 0b00000001, 2 << 0b1111111101000001)
 * ```
 */
class VerboseBitShiftMethodTransformer : PureMethodTransformer() {

    override fun transform(item: MethodNode, context: MethodContext) {
        val matches = item.match(BIT_SHIFT_PATTERN)
        var simplified = 0

        for (match in matches) {
            val (push, shift) = match
            val bits = push.getNumericPushValue()

            val max = if (shift.opcode in LONG_SHIFT_OPCODES) Long.SIZE_BITS else Int.SIZE_BITS
            if (bits in 0 until max) {
                continue
            }

            val constrained = bits.toInt() and (max - 1)
            item.instructions[push] = constrained.toPushInstruction()

            simplified++
            logger.debug { "Simplifying shift from $bits to $constrained in ${context.printableName(item)}" }
        }

        if (simplified > 0) {
            logger.info { "Simplified $simplified bitshifts in ${context.printableName(item)}" }
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger { }

        private const val PUSH_NUMBER = "(ICONST | BIPUSH | SIPUSH | LDC)"
        private const val SHIFT_OP = "(ISHL | ISHR | IUSHR | LSHL | LSHR | LUSHR)"

        private val BIT_SHIFT_PATTERN = InstructionPattern.compile("$PUSH_NUMBER $SHIFT_OP")

        private val LONG_SHIFT_OPCODES = hashSetOf(Opcodes.LSHL, Opcodes.LSHR, Opcodes.LUSHR)
    }

}