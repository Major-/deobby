package rs.eumulate.deobby.asm

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode

/**
 * Creates a numeric push instruction.
 */
fun Int.toPushInstruction(): AbstractInsnNode {
    return when (this) {
        -1 -> InsnNode(ICONST_M1)
        0 -> InsnNode(ICONST_0)
        1 -> InsnNode(ICONST_1)
        2 -> InsnNode(ICONST_2)
        3 -> InsnNode(ICONST_3)
        4 -> InsnNode(ICONST_4)
        5 -> InsnNode(ICONST_5)
        in Byte.MIN_VALUE..Byte.MAX_VALUE -> IntInsnNode(BIPUSH, this)
        in Short.MIN_VALUE..Short.MAX_VALUE -> IntInsnNode(SIPUSH, this)
        else -> LdcInsnNode(this)
    }
}

/**
 * Creates a numeric push instruction.
 */
fun Long.toPushInstruction(): AbstractInsnNode {
    return when (this) {
        -1L -> InsnNode(ICONST_M1)
        0L -> InsnNode(ICONST_0)
        1L -> InsnNode(ICONST_1)
        2L -> InsnNode(ICONST_2)
        3L -> InsnNode(ICONST_3)
        4L -> InsnNode(ICONST_4)
        5L -> InsnNode(ICONST_5)
        in Byte.MIN_VALUE..Byte.MAX_VALUE -> IntInsnNode(BIPUSH, toInt())
        in Short.MIN_VALUE..Short.MAX_VALUE -> IntInsnNode(SIPUSH, toInt())
        in Int.MIN_VALUE..Int.MAX_VALUE -> LdcInsnNode(toInt())
        else -> LdcInsnNode(this)
    }
}

/**
 * Gets the value of a numeric push instruction (which can be an `ICONST_*`, `BIPUSH`, `SIPUSH`, or `LDC_*`
 * instruction.
 */
fun AbstractInsnNode.getNumericPushValue(): Long {
    return when (this) {
        is InsnNode -> when (getOpcode()) {
            ICONST_M1 -> -1
            ICONST_0 -> 0
            ICONST_1 -> 1
            ICONST_2 -> 2
            ICONST_3 -> 3
            ICONST_4 -> 4
            ICONST_5 -> 5
            else -> throw IllegalArgumentException("Cannot derive a numeric push value from ${javaClass.name}")
        }
        is IntInsnNode -> operand.toLong()
        is LdcInsnNode -> (cst as Number).toLong()
        else -> throw IllegalArgumentException("Cannot derive a numeric push value from ${javaClass.name}")
    }
}

fun AbstractInsnNode.nextInsnNode(): AbstractInsnNode? {
    var node = next
    while (node != null && node.opcode == -1) {
        node = node.next
    }

    return node
}