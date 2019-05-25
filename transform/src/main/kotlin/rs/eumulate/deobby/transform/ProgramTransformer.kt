package rs.eumulate.deobby.transform

interface ProgramTransformer : Transformer<Program> {

    override fun transform(item: Program)

}