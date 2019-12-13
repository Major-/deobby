package rs.eumulate.deobby.asm.wrapper

import org.objectweb.asm.tree.*

fun AbstractInsnNode.equivalentTo(other: AbstractInsnNode): Boolean {
    if (opcode != other.opcode) {
        return false
    }

    return when (this) {
        is InsnNode -> true // already checked the opcode
        is FieldInsnNode -> equivalentTo(other as FieldInsnNode)
        is FrameNode -> type == other.type && equivalentTo(other as FrameNode)
        is IincInsnNode -> equivalentTo(other as IincInsnNode)
        is IntInsnNode -> equivalentTo(other as IntInsnNode)
        is InvokeDynamicInsnNode -> equivalentTo(other as InvokeDynamicInsnNode)
        is JumpInsnNode -> equivalentTo(other as JumpInsnNode)
        is LabelNode -> type == other.type && equivalentTo(other as LabelNode)
        is LdcInsnNode -> equivalentTo(other as LdcInsnNode)
        is LineNumberNode -> type == other.type && equivalentTo(other as LineNumberNode)
        is LookupSwitchInsnNode -> equivalentTo(other as LookupSwitchInsnNode)
        is MethodInsnNode -> equivalentTo(other as MethodInsnNode)
        is MultiANewArrayInsnNode -> equivalentTo(other as MultiANewArrayInsnNode)
        is TableSwitchInsnNode -> equivalentTo(other as TableSwitchInsnNode)
        is TypeInsnNode -> equivalentTo(other as TypeInsnNode)
        is VarInsnNode -> equivalentTo(other as VarInsnNode)
        else -> error("Unrecognised instruction node ${this.javaClass.simpleName}")
    }
}

fun FieldInsnNode.equivalentTo(other: FieldInsnNode): Boolean {
    return owner == other.owner && name == other.name && desc == other.desc
}

@Suppress("unused", "UNUSED_PARAMETER")
fun FrameNode.equivalentTo(other: FrameNode): Boolean {
    TODO("unimplemented")
}

fun IincInsnNode.equivalentTo(other: IincInsnNode): Boolean {
    return `var` == other.`var` && incr == other.incr
}

fun IntInsnNode.equivalentTo(other: IntInsnNode): Boolean {
    return operand == other.operand
}

fun InvokeDynamicInsnNode.equivalentTo(other: InvokeDynamicInsnNode): Boolean {
    return name == other.name && desc == other.desc && bsm == other.bsm && bsmArgs.contentEquals(other.bsmArgs)
}

fun JumpInsnNode.equivalentTo(other: JumpInsnNode): Boolean {
    return label.equivalentTo(other.label)
}

fun LabelNode.equivalentTo(other: LabelNode): Boolean {
    return label.offset == other.label.offset // TODO is this appropriate?
}

fun LdcInsnNode.equivalentTo(other: LdcInsnNode): Boolean {
    return cst == other.cst
}

fun LineNumberNode.equivalentTo(other: LineNumberNode): Boolean {
    return start.equivalentTo(other.start)
}

fun LookupSwitchInsnNode.equivalentTo(other: LookupSwitchInsnNode): Boolean {
    return dflt.equivalentTo(other.dflt) &&
        keys == other.keys &&
        labels.size == other.labels.size &&
        labels.withIndex().all { (index, label) -> label.equivalentTo(other.labels[index]) }
}

fun MethodInsnNode.equivalentTo(other: MethodInsnNode): Boolean {
    return owner == other.owner && name == other.name && desc == other.desc
}

fun MultiANewArrayInsnNode.equivalentTo(other: MultiANewArrayInsnNode): Boolean {
    return desc == other.desc && dims == other.dims
}

fun TableSwitchInsnNode.equivalentTo(other: TableSwitchInsnNode): Boolean {
    return dflt.equivalentTo(other.dflt) &&
        min == other.min &&
        max == other.max &&
        labels.size == other.labels.size &&
        labels.withIndex().all { (index, label) -> label.equivalentTo(other.labels[index]) }
}

fun TypeInsnNode.equivalentTo(other: TypeInsnNode): Boolean {
    return desc == other.desc
}

fun VarInsnNode.equivalentTo(other: VarInsnNode): Boolean {
    return `var` == other.`var`
}