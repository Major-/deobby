package rs.eumulate.deobby.asm

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode

/**
 * Creates a numeric push instruction.
 */
fun Long.toPushInstruction(): AbstractInsnNode {
    return if (this == -1L) {
        InsnNode(Opcodes.ICONST_M1)
    } else if (this == 0L) {
        InsnNode(Opcodes.ICONST_0)
    } else if (this == 1L) {
        InsnNode(Opcodes.ICONST_1)
    } else if (this == 2L) {
        InsnNode(Opcodes.ICONST_2)
    } else if (this == 3L) {
        InsnNode(Opcodes.ICONST_3)
    } else if (this == 4L) {
        InsnNode(Opcodes.ICONST_4)
    } else if (this == 5L) {
        InsnNode(Opcodes.ICONST_5)
    } else if (this >= java.lang.Byte.MIN_VALUE && this <= java.lang.Byte.MAX_VALUE) {
        IntInsnNode(Opcodes.BIPUSH, toInt())
    } else if (this >= java.lang.Short.MIN_VALUE && this <= java.lang.Short.MAX_VALUE) {
        IntInsnNode(Opcodes.SIPUSH, toInt())
    } else if (this >= Integer.MIN_VALUE && this <= Integer.MAX_VALUE) {
        LdcInsnNode(toInt())
    } else {
        LdcInsnNode(this)
    }
}

/**
 * Gets the value of a numeric push instruction (which can be an `ICONST_*`, `BIPUSH`, `SIPUSH`, or `LDC_*`
 * instruction.
 */
fun AbstractInsnNode.getNumericPushValue(): Long {
    return when (this) {
        is InsnNode -> when (getOpcode()) {
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