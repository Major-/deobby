package rs.eumulate.deobby.transform

import org.objectweb.asm.tree.MethodNode
import rs.eumulate.deobby.asm.tree.printableName

/**
 * A [Transformer] that manipulates [MethodNode]s and may have [Program]-wide side-effects.
 */
interface MethodTransformer : Transformer<MethodNode, MethodContext> {

    /**
     * Initialise this [MethodTransformer] using information from the specified [Program].
     *
     * This function may be executed in parallel and thus **must not** mutate the [Program].
     */
    fun initialise(program: Program)

    /**
     * Perform clean-up after this [MethodTransformer] has been executed on all methods, e.g. to remove fields that are
     * no longer used.
     *
     * This function **may** mutate the [Program].
     */
    fun finish(program: Program)

}

/**
 * A [MethodTransformer] that visits a single method and has no external side-effects.
 */
abstract class PureMethodTransformer : MethodTransformer {

    final override fun initialise(program: Program) {
        /* do nothing */
    }

    final override fun finish(program: Program) {
        /* do nothing */
    }

}

data class MethodContext(val className: String) : TransformerContext {

    fun printableName(method: MethodNode): String {
        return "${className}/${method.printableName}"
    }

}
