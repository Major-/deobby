package rs.emulate.deobby.transform.exception

import com.github.michaelbull.logging.InlineLogger
import org.objectweb.asm.tree.MethodNode
import rs.emulate.deobby.asm.match.InstructionMatcher
import rs.emulate.deobby.asm.match.InstructionPattern
import rs.emulate.deobby.asm.tree.nextInsnNode
import rs.emulate.deobby.transform.MethodContext
import rs.emulate.deobby.transform.PureMethodTransformer

class ExceptionTracingTransformer : PureMethodTransformer() {

    override fun transform(item: MethodNode, context: MethodContext) {
        val matcher = InstructionMatcher(item.instructions)
        var removed = 0

        for (match in matcher.match(EXCEPTION_PATTERN)) {
            val found = item.tryCatchBlocks.removeIf { tryCatch ->
                tryCatch.type == "java/lang/RuntimeException" && tryCatch.handler.nextInsnNode() === match.first()
            }

            if (found) {
                match.forEach(item.instructions::remove)
                removed++
            }
        }

        if (removed > 0) {
            logger.info { "Removed $removed catch blocks from ${context.printableName(item)}" }
        }
    }

    companion object {
        private val logger = InlineLogger()

        private val EXCEPTION_PATTERN = InstructionPattern.compile(
            """
            ASTORE
            ALOAD
            (| LDC INVOKESTATIC |
                NEW DUP
                (LDC INVOKESPECIAL | INVOKESPECIAL LDC INVOKEVIRTUAL)
                ((ILOAD | LLOAD | FLOAD | DLOAD | (ALOAD IFNULL LDC GOTO LDC) | BIPUSH) INVOKEVIRTUAL)*
                INVOKEVIRTUAL INVOKESTATIC
            )
            ATHROW
            """
        )
    }
}