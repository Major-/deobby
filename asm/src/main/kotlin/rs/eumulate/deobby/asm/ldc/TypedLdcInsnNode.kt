@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package rs.eumulate.deobby.asm.ldc

import org.objectweb.asm.Type
import org.objectweb.asm.tree.LdcInsnNode

inline fun IntLdcInsnNode(value: Int): LdcInsnNode = LdcInsnNode(value)

inline fun FloatLdcInsnNode(value: Float): LdcInsnNode = LdcInsnNode(value)

inline fun LongLdcInsnNode(value: Long): LdcInsnNode = LdcInsnNode(value)

inline fun DoubleLdcInsnNode(value: Double): LdcInsnNode = LdcInsnNode(value)

inline fun StringLdcInsnNode(value: String): LdcInsnNode = LdcInsnNode(value)

inline fun TypeLdcInsnNode(value: Type): LdcInsnNode = LdcInsnNode(value)

fun LdcInsnNode.isInt(): Boolean = cst is Int

fun LdcInsnNode.isFloat(): Boolean = cst is Float

fun LdcInsnNode.isLong(): Boolean = cst is Long

fun LdcInsnNode.isDouble(): Boolean = cst is Double

fun LdcInsnNode.isString(): Boolean = cst is String

fun LdcInsnNode.isType(): Boolean = cst is Type
