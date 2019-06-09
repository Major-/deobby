package rs.emulate.deobby.transform

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode
import rs.eumulate.deobby.asm.wrapper.Assertions
import rs.eumulate.deobby.transform.MethodContext
import rs.eumulate.deobby.transform.PureMethodTransformer

abstract class PureMethodTransformerTest {

    protected abstract val transformer: PureMethodTransformer

    @Suppress("NOTHING_TO_INLINE") // inline the function so we get the correct class name
    protected inline fun assertInstructionEquals(expected: Array<AbstractInsnNode>, actual: Array<AbstractInsnNode>) {
        assertInstructionEquals(expected, actual, Thread.currentThread().stackTrace[1].methodName)
    }

    protected fun assertInstructionEquals(
        expected: Array<AbstractInsnNode>,
        actual: Array<AbstractInsnNode>,
        name: String
    ) {
        val node = createMethodNode(name, actual)
        val context = MethodContext(javaClass.name)

        transformer.transform(node, context)

        Assertions.assertInstructionEquals(expected, node.instructions)
    }

    private fun createMethodNode(name: String, instructions: Array<AbstractInsnNode>): MethodNode {
        return MethodNode().also {
            it.name = "test function: `$name`"
            it.desc = "()V"
            it.instructions = InsnList().apply { instructions.forEach(::add) }
        }
    }

}