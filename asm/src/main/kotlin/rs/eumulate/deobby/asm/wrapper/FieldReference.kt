package rs.eumulate.deobby.asm.wrapper

import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode

/**
 * A reference to a [FieldNode] that can be used in situations when field identity (i.e. [equals] and [hashCode]
 * implementations) is required.
 */
data class FieldReference(val owner: String, val name: String, val desc: String)

/**
 * The pair of values that identify a field in a class.
 */
data class FieldId(val name: String, val desc: String)

/**
 * Creates a [FieldReference] that can be used to determine field equality.
 */
fun FieldInsnNode.asReference(): FieldReference {
    return FieldReference(owner, name, desc)
}

/**
 * Creates a [FieldId] that can be used to determine identify a field in a class.
 */
fun FieldInsnNode.asId(): FieldId {
    return FieldId(name, desc)
}

fun FieldNode.referencedBy(reference: FieldReference): Boolean {
    return name == reference.name && desc == reference.desc
}