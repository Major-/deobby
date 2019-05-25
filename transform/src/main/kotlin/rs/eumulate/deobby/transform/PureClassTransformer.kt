package rs.eumulate.deobby.transform

import org.objectweb.asm.tree.ClassNode

/**
 * A [Transformer] that visits a single class and has no external side-effects.
 */
interface PureClassTransformer : Transformer<ClassNode> {

    override fun transform(item: ClassNode)

}