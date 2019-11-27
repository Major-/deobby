package rs.eumulate.deobby.transform

import rs.eumulate.deobby.asm.tree.hasBytecode

/**
 * A collection of [Transformer]s applied in order.
 */
class TransformerSet( // TODO need more control over the transformer order than this
    private val programTransformers: List<ProgramTransformer> = emptyList(),
    private val classTransformers: List<ClassTransformer> = emptyList(),
    private val methodTransformers: List<MethodTransformer> = emptyList()
) : ProgramTransformer {

    override fun transform(item: Program, context: ProgramContext) {
        programTransformers.forEach { it.transform(item, context) }

        methodTransformers.forEach { it.initialise(item) }

        for (clazz in item.classes()) {
            classTransformers.forEach { it.transform(clazz, ClassContext) }

            val methodContext = MethodContext(clazz.name)

            for (method in clazz.methods) {
                if (method.hasBytecode()) {
                    methodTransformers.forEach { it.transform(method, methodContext) }
                }
            }
        }

        methodTransformers.forEach { it.finish(item) }
    }

}