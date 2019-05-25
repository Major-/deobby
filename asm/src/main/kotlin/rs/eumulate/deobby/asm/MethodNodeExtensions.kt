package rs.eumulate.deobby.asm

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode

fun MethodNode.isPublic(): Boolean {
    return access and Opcodes.ACC_PUBLIC != 0
}

fun MethodNode.isProtected(): Boolean {
    return access and Opcodes.ACC_PROTECTED != 0
}

fun MethodNode.isPrivate(): Boolean {
    return access and Opcodes.ACC_PRIVATE != 0
}

fun MethodNode.isStatic(): Boolean {
    return access and Opcodes.ACC_STATIC != 0
}

fun MethodNode.isFinal(): Boolean {
    return access and Opcodes.ACC_FINAL != 0
}

fun MethodNode.isSynchronized(): Boolean {
    return access and Opcodes.ACC_SYNCHRONIZED != 0
}

fun MethodNode.isAbstract(): Boolean {
    return access and Opcodes.ACC_ABSTRACT != 0
}

fun MethodNode.isNative(): Boolean {
    return access and Opcodes.ACC_NATIVE != 0
}