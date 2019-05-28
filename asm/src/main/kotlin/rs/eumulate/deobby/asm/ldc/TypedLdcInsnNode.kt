@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package rs.eumulate.deobby.asm.ldc

import org.objectweb.asm.Type
import org.objectweb.asm.tree.LdcInsnNode

inline fun IntLdcInsnNode(value: Int) = LdcInsnNode(value)

inline fun FloatLdcInsnNode(value: Float) = LdcInsnNode(value)

inline fun LongLdcInsnNode(value: Long) = LdcInsnNode(value)

inline fun DoubleLdcInsnNode(value: Double) = LdcInsnNode(value)

inline fun StringLdcInsnNode(value: String) = LdcInsnNode(value)

inline fun TypeLdcInsnNode(value: Type) = LdcInsnNode(value)
