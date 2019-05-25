package rs.eumulate.deobby.transform

import org.objectweb.asm.tree.MethodNode

/**
 * A [Transformer] that visits a single method and has no external side-effects.
 */
interface PureMethodTransformer : Transformer<MethodNode> {

    override fun transform(item: MethodNode)

}