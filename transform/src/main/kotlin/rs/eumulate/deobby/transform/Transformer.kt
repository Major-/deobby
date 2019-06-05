package rs.eumulate.deobby.transform

interface Transformer<T, C : TransformerContext> {

    fun transform(item: T, context: C)

}

interface TransformerContext