package rs.eumulate.deobby.asm.match

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.util.Printer
import java.util.regex.Matcher
import java.util.regex.Pattern

typealias InstructionPatternMatch = List<AbstractInsnNode>

inline class InstructionPattern(val pattern: Pattern) {

    fun matcher(input: CharSequence): Matcher {
        return pattern.matcher(input)
    }

    companion object {

        /**
         * Converts a readable pattern which uses instruction mnemonics to the internal character-based format used
         * when actually attempting to find matches.
         */
        fun compile(expression: String): InstructionPattern {
            val regex = StringBuilder()
            val name = StringBuilder()

            for (character in expression) {
                if (Character.isLetterOrDigit(character) || character == '_') {
                    name.append(character)
                } else {
                    if (name.isNotEmpty()) {
                        regex.append(instructionToString(name.toString()))
                        name.clear()
                    }

                    if (!Character.isWhitespace(character)) {
                        regex.append(character)
                    }
                }
            }

            if (name.isNotEmpty()) {
                regex.append(instructionToString(name.toString()))
            }

            val pattern = Pattern.compile(regex.toString())
            return InstructionPattern(pattern)
        }

        /**
         * Contains groups of instructions which are converted to the appropriate regular expression automatically.
         */
        private val groups = mutableMapOf(
            "insnnode" to intArrayOf(
                NOP, ACONST_NULL,
                ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1,
                FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1,
                IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD,
                IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE,
                POP, POP2,
                DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2,
                SWAP,
                IADD, LADD, FADD, DADD,
                ISUB, LSUB, FSUB, DSUB,
                IMUL, LMUL, FMUL, DMUL,
                IDIV, LDIV, FDIV, DDIV,
                IREM, LREM, FREM, DREM,
                INEG, LNEG, FNEG, DNEG,
                ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR,
                IAND, LAND, IOR, LOR, IXOR, LXOR,
                I2L, I2F, I2D, L2I, L2F, L2D,
                F2I, F2L, F2D, D2I, D2L, D2F,
                I2B, I2C, I2S,
                LCMP, FCMPL, FCMPG, DCMPL, DCMPG,
                IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN,
                ARRAYLENGTH,
                ATHROW,
                MONITORENTER, MONITOREXIT
            ),

            "intinsnnode" to intArrayOf(BIPUSH, SIPUSH, NEWARRAY),
            "varinsnnode" to intArrayOf(
                ILOAD, LLOAD, FLOAD, DLOAD, ALOAD,
                ISTORE, LSTORE, FSTORE, DSTORE, ASTORE,
                RET
            ),

            "typeinsnnode" to intArrayOf(NEW, ANEWARRAY, CHECKCAST, INSTANCEOF),
            "fieldinsnnode" to intArrayOf(GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD),

            "methodinsnnode" to intArrayOf(
                INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE, INVOKEDYNAMIC
            ),

            "jumpinsnnode" to intArrayOf(
                IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE,
                IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
                GOTO, JSR,
                IFNULL, IFNONNULL
            ),

            "ldcinsnnode" to intArrayOf(LDC),
            "iincinsnnode" to intArrayOf(IINC),
            "tableswitchinsnnode" to intArrayOf(TABLESWITCH),
            "lookupswitchinsnnode" to intArrayOf(LOOKUPSWITCH),
            "multianewarrayinsnnode" to intArrayOf(MULTIANEWARRAY),

            "iconst" to intArrayOf(ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5),

            "pushinstruction" to intArrayOf(
                ACONST_NULL,
                ALOAD, ILOAD, LLOAD, FLOAD, DLOAD,
                BIPUSH, SIPUSH,
                LDC,
                DUP, DUP2,
                GETSTATIC,
                LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1
            ),

            "invokeinstruction" to intArrayOf(
                INVOKEDYNAMIC, INVOKEINTERFACE, INVOKESPECIAL, INVOKESTATIC, INVOKEVIRTUAL
            ),

            "ifinstruction" to intArrayOf(
                IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE,
                IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
                IFNULL, IFNONNULL
            )
        )

        private val opcodeMapping = Printer.OPCODES.withIndex().associateBy({ it.value }) { it.index }

        /**
         * Converts an instruction to a string, used to build the regular expression.
         * @throws IllegalArgumentException If the instruction does not exist.
         */
        internal fun instructionToString(name: String): String {
            val uppercased = name.toUpperCase()
            val lowercased = name.toLowerCase()

            return when {
                uppercased in opcodeMapping -> opcodeToString(opcodeMapping.getValue(uppercased))
                lowercased in groups -> groups[lowercased]!!.joinToString(
                    separator = "|",
                    prefix = "(",
                    postfix = ")",
                    transform = ::opcodeToString
                )
                lowercased == "abstractinsnnode" -> "."
                else -> throw IllegalArgumentException("$name is not a known instruction.")
            }
        }

        /**
         * Converts an opcode to a string. This adds 0xE000 to the operation code so that the character(s) are in the
         * Unicode Private Use Area.
         */
        internal fun opcodeToString(opcode: Int): String {
            return (0xE000 + opcode).toChar().toString()
        }

    }

}
