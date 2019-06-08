package rs.eumulate.deobby.transform

interface Transformer<T, C : TransformerContext> {

    /**
     * Transforms the [item]. May be executed in parallel (on different [item]s) and thus must not mutate anything
     * other than the provided [item].
     */
    fun transform(item: T, context: C)

}

interface TransformerContext