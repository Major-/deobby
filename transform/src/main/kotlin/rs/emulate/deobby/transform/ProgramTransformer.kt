package rs.emulate.deobby.transform

import java.nio.file.Path

interface ProgramTransformer : Transformer<Program, ProgramContext> {

    override fun transform(item: Program, context: ProgramContext)

}

data class ProgramContext(val path: Path) : TransformerContext