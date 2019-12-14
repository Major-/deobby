package rs.eumulate.deobby.asm

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import rs.eumulate.deobby.asm.ldc.IntLdcInsnNode
import rs.eumulate.deobby.asm.ldc.LongLdcInsnNode

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
        else -> IntLdcInsnNode(this)
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
        in Int.MIN_VALUE..Int.MAX_VALUE -> IntLdcInsnNode(toInt())
        else -> LongLdcInsnNode(this)
    }
}