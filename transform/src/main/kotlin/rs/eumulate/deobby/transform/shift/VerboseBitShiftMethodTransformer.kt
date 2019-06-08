package rs.eumulate.deobby.transform.shift

import mu.KotlinLogging
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode
import rs.eumulate.deobby.asm.InstructionMatcher
import rs.eumulate.deobby.asm.InstructionPattern
import rs.eumulate.deobby.asm.toPushInstruction
import rs.eumulate.deobby.asm.tree.getNumericPushValue
import rs.eumulate.deobby.asm.tree.printableName
import rs.eumulate.deobby.transform.MethodContext
import rs.eumulate.deobby.transform.PureMethodTransformer

class VerboseBitShiftMethodTransformer : PureMethodTransformer() {

    override fun transform(item: MethodNode, context: MethodContext) {
        val matcher = InstructionMatcher(item.instructions)
        val matches = matcher.match(BIT_SHIFT_PATTERN)

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
            logger.debug { "Simplifying shift from $bits to $constrained in ${context.className}.${item.printableName}" }
        }

        if (simplified > 0) {
            logger.info { "Simplified $simplified bitshifts in ${context.className}.${item.printableName}" }
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger { }

        private const val PUSH_NUMBER = "(ICONST | BIPUSH | SIPUSH | LDC)"
        private const val SHIFT_OP = "(ISHL | ISHR | IUSHR | LSHL | LSHR | LUSHR)"

        private val BIT_SHIFT_PATTERN = InstructionPattern.compile("$PUSH_NUMBER $SHIFT_OP")

        private val LONG_SHIFT_OPCODES = setOf(Opcodes.LSHL, Opcodes.LSHR, Opcodes.LUSHR)
    }

}