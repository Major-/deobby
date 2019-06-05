package rs.eumulate.deobby.asm.tree

import org.objectweb.asm.tree.MethodNode

val MethodNode.printableName: String
    get() = name + desc