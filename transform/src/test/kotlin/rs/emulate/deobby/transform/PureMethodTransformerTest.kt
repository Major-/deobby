package rs.emulate.deobby.transform

import org.junit.jupiter.api.Assertions.assertEquals
import org.objectweb.asm.tree.*
import rs.eumulate.deobby.transform.PureMethodTransformer

abstract class PureMethodTransformerTest {

    protected abstract val transformer: PureMethodTransformer

    protected fun assertInstructionEquals(expected: Array<AbstractInsnNode>, input: () -> Array<AbstractInsnNode>) {
        val node = createMethodNode(input)
        transformer.transform(node)

        assertInstructionEquals(expected, node.instructions)
    }

    protected fun assertInstructionEquals(expected: Array<AbstractInsnNode>, actual: InsnList) {
        assertEquals(expected.size, actual.size()) { "Array size mismatch" }

        loop@ for (index in expected.indices) {
            val left = expected[index]
            val right = actual[index]

            assertEquals(left.opcode, right.opcode) { "Opcode mismatch (index=$index)" }

            when (left) {
                is FieldInsnNode -> check(left, right, { owner }, { name }, { desc })
                is FrameNode -> check(left, right, { type }, { local }, { stack })
                is InsnNode -> continue@loop /* has no additional operands */
                is IincInsnNode -> check(left, right, { `var` }, { incr })
                is IntInsnNode -> check(left, right, { operand })
                is InvokeDynamicInsnNode -> throw UnsupportedOperationException("InvokeDynamicInsnNode not supported")
                is JumpInsnNode -> check(left, right, { label })
                is LabelNode -> check(left, right, { label })
                is LdcInsnNode -> check(left, right, { cst })
                is LineNumberNode -> throw UnsupportedOperationException("LineNumberNode not supported")
                is LookupSwitchInsnNode -> throw UnsupportedOperationException("LookupSwitchInsnNode not supported")
                is MethodInsnNode -> check(left, right, { owner }, { name }, { desc }, { itf })
                is MultiANewArrayInsnNode -> check(left, right, { desc }, { dims })
                is TableSwitchInsnNode -> throw UnsupportedOperationException("TableSwitchInsnNode not supported")
                is TypeInsnNode -> check(left, right, { desc })
                is VarInsnNode -> check(left, right, { `var` })
            }
        }
    }

    private inline fun <reified T : AbstractInsnNode> check(
        first: T,
        second: AbstractInsnNode,
        vararg values: T.() -> Any?
    ) {
        val casted = requireNotNull(second as? T) {
            "Expected node of type ${T::class.java.name}, was ${second::class.java.name}"
        }

        values.forEach { assertEquals(first.it(), casted.it()) }
    }

    private fun createMethodNode(nodes: () -> Array<AbstractInsnNode>): MethodNode {
        val methodName = nodes.javaClass.simpleName.substringBefore("$")

        return MethodNode().apply {
            name = "test function: `$methodName`"
            signature = "()V"
            instructions = InsnList().apply { nodes().forEach(::add) }
        }
    }

}