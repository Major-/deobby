package rs.eumulate.deobby.transform

import org.objectweb.asm.tree.ClassNode

/**
 * A [Transformer] that visits a single class and has no external side-effects.
 */
interface PureClassTransformer : Transformer<ClassNode, ClassContext> {

    override fun transform(item: ClassNode, context: ClassContext)

}

object ClassContext : TransformerContext // TODO what to include here
