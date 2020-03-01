package rs.emulate.deobby.asm.tree

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode

/**
 * Gets the value of a numeric push instruction (which can be an `ICONST_*`, `BIPUSH`, `SIPUSH`, or `LDC_*`
 * instruction.
 */
fun AbstractInsnNode.getNumericPushValue(): Long {
    return when (this) {
        is InsnNode -> when (opcode) {
            Opcodes.ICONST_M1 -> -1
            Opcodes.ICONST_0 -> 0
            Opcodes.ICONST_1 -> 1
            Opcodes.ICONST_2 -> 2
            Opcodes.ICONST_3 -> 3
            Opcodes.ICONST_4 -> 4
            Opcodes.ICONST_5 -> 5
            else -> throw IllegalArgumentException("Cannot derive a numeric push value from ${javaClass.name}")
        }
        is IntInsnNode -> operand.toLong()
        is LdcInsnNode -> (cst as Number).toLong()
        else -> throw IllegalArgumentException("Cannot derive a numeric push value from ${javaClass.name}")
    }
}

/**
 * Returns the next non-pseduo (i.e. `opcode != -1`) [AbstractInsnNode], or `null` if one does not exist.
 */
fun AbstractInsnNode.nextInsnNode(): AbstractInsnNode? {
    var node = next
    while (node != null && node.opcode == -1) {
        node = node.next
    }

    return node
}