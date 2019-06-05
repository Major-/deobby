package rs.eumulate.deobby.transform

import org.objectweb.asm.tree.MethodNode

/**
 * A [Transformer] that visits a single method and has no external side-effects.
 */
interface PureMethodTransformer : Transformer<MethodNode, MethodContext> {

    override fun transform(item: MethodNode, context: MethodContext)

}

data class MethodContext(val className: String) : TransformerContext
