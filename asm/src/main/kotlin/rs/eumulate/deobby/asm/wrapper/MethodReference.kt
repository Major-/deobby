package rs.eumulate.deobby.asm.wrapper

import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

/**
 * A reference to a [MethodNode] that can be used in situations when method identity (i.e. [equals] and [hashCode]
 * implementations) is required.
 */
data class MethodReference(val owner: String, val name: String, val desc: String)

/**
 * The pair of values that identify a method in a class.
 */
data class MethodId(val name: String, val desc: String)

/**
 * Creates a [MethodReference] that can be used to determine method equality.
 */
fun MethodInsnNode.asReference(): MethodReference {
    return MethodReference(owner, name, desc)
}

/**
 * Creates a [MethodId] that can be used to determine identify a method in a class.
 */
fun MethodInsnNode.asId(): MethodId {
    return MethodId(name, desc)
}

fun MethodNode.referencedBy(reference: MethodReference): Boolean {
    return name == reference.name && desc == reference.desc
}