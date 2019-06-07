package rs.eumulate.deobby.asm.tree

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

fun InsnList.remove(vararg nodes: AbstractInsnNode) {
    nodes.forEach(::remove)
}