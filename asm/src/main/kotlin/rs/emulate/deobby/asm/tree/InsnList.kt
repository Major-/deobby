package rs.emulate.deobby.asm.tree

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

/**
 * Removes each of the [nodes], in order, from this [InsnList].
 */
fun InsnList.remove(vararg nodes: AbstractInsnNode) {
    nodes.forEach(::remove)
}

/**
 * Removes all [AbstractInsnNode]s, including psuedonodes, between [start] and [end] **inclusive**.
 *
 * TODO optimise to avoid the unnecessary relinking
 */
fun InsnList.removeRange(start: AbstractInsnNode, end: AbstractInsnNode) {
    var current = start
    while (current != end) {
        val next = checkNotNull(current.next) { "Failed to find end node $end before InsnList ended." }
        remove(current) // clears start.next
        current = next
    }

    remove(current)
}