package rs.eumulate.deobby.transform

import rs.eumulate.deobby.asm.isAbstract
import rs.eumulate.deobby.asm.isNative

/**
 * A collection of [Transformer]s applied in order.
 */
class TransformerSet( // TODO need more control over the transformer order than this
    private val programTransformers: List<ProgramTransformer> = emptyList(),
    private val classTransformers: List<PureClassTransformer> = emptyList(),
    private val methodTransformers: List<PureMethodTransformer> = emptyList()
) : ProgramTransformer {

    override fun transform(item: Program) {
        programTransformers.forEach { it.transform(item) }

        for (clazz in item.classes) {
            classTransformers.forEach { it.transform(clazz) }

            for (method in clazz.methods) {
                if (!method.isNative() && !method.isAbstract())
                    methodTransformers.forEach { it.transform(method) }
            }
        }
    }

}