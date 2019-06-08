package rs.eumulate.deobby.transform

import org.objectweb.asm.tree.ClassNode

/**
 * A [Transformer] that manipulates [ClassNode]s and may have [Program]-wide side-effects.
 */
interface ClassTransformer : Transformer<ClassNode, ClassContext> {

    /**
     * Initialise this [ClassTransformer] using information from the specified [Program].
     *
     * This function may be executed in parallel and thus **must not** mutate the [Program].
     */
    fun initialise(program: Program)

    /**
     * Perform clean-up after this [ClassTransformer] has been executed on all classes, e.g. to remove fields that are
     * no longer used.
     *
     * This function **may** mutate the [Program].
     */
    fun finish(program: Program)

}

/**
 * A [Transformer] that visits a single class and has no external side-effects.
 */
abstract class PureClassTransformer : ClassTransformer {

    final override fun initialise(program: Program) {
        /* do nothing */
    }

    final override fun finish(program: Program) {
        /* do nothing */
    }

}

object ClassContext : TransformerContext // TODO what to include here
