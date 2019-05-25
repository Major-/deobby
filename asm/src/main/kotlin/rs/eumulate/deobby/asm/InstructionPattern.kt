package rs.eumulate.deobby.asm

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.util.Printer
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

typealias InstructionPatternMatch = List<AbstractInsnNode>

inline class InstructionPattern(val pattern: Pattern) {

    fun matcher(input: CharSequence): Matcher {
        return pattern.matcher(input)
    }

    companion object {

        /**
         * Contains groups of instructions which are converted to the appropriate regular expression automatically.
         */
        private val groups = HashMap<String, IntArray>().also {
            it["insnnode"] = intArrayOf(
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
                IAND, LAND,
                IOR, LOR,
                IXOR, LXOR,
                I2L, I2F, I2D,
                L2I, L2F, L2D,
                F2I, F2L, F2D,
                D2I, D2L, D2F,
                I2B, I2C, I2S,
                LCMP, FCMPL, FCMPG, DCMPL, DCMPG,
                IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN,
                ARRAYLENGTH,
                ATHROW,
                MONITORENTER, MONITOREXIT
            )

            it["intinsnnode"] = intArrayOf(BIPUSH, SIPUSH, NEWARRAY)
            it["varinsnnode"] = intArrayOf(
                ILOAD, LLOAD, FLOAD, DLOAD, ALOAD,
                ISTORE, LSTORE, FSTORE, DSTORE, ASTORE,
                RET
            )

            it["typeinsnnode"] = intArrayOf(NEW, ANEWARRAY, CHECKCAST, INSTANCEOF)
            it["fieldinsnnode"] = intArrayOf(GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD)

            it["methodinsnnode"] = intArrayOf(
                INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE, INVOKEDYNAMIC
            )

            it["jumpinsnnode"] = intArrayOf(
                IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE,
                IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
                GOTO, JSR,
                IFNULL, IFNONNULL
            )

            it["ldcinsnnode"] = intArrayOf(LDC)
            it["iincinsnnode"] = intArrayOf(IINC)
            it["tableswitchinsnnode"] = intArrayOf(TABLESWITCH)
            it["lookupswitchinsnnode"] = intArrayOf(LOOKUPSWITCH)
            it["multianewarrayinsnnode"] = intArrayOf(MULTIANEWARRAY)

            it["iconst"] = intArrayOf(ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5)

            it["pushinstruction"] = intArrayOf(
                ACONST_NULL,
                ALOAD, ILOAD, LLOAD, FLOAD, DLOAD,
                BIPUSH, SIPUSH,
                LDC,
                DUP, DUP2,
                GETSTATIC,
                LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1
            )

            it["invokeinstruction"] = intArrayOf(
                INVOKEDYNAMIC, INVOKEINTERFACE, INVOKESPECIAL, INVOKESTATIC, INVOKEVIRTUAL
            )

            it["ifinstruction"] = intArrayOf(
                IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE,
                IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
                IFNULL, IFNONNULL
            )
        }

        /**
         * Converts an instruction to character(s) used to build the regular expression.
         * @param name The name of the instruction.
         * @return The character(s) which represents this instruction.
         * @throws IllegalArgumentException If the instruction does not exist.
         */
        internal fun instructionToString(name: String): String {
            for (index in 0 until Printer.OPCODES.size) {
                if (name.equals(Printer.OPCODES[index], ignoreCase = true)) {
                    return opcodeToString(index)
                }
            }

            val group = groups[name.toLowerCase()]
            if (group != null) {
                return group.joinToString(separator = "|", prefix = "(", postfix = ")", transform = ::opcodeToString)
            } else if (name.equals("AbstractInsnNode", ignoreCase = true)) {
                return "."
            }

            throw IllegalArgumentException("$name is not a known instruction.")
        }

        /**
         * Converts an opcode to a string. This adds 0xE000 to the operation code so that the character is in the
         * "Private Use Area".
         */
        fun opcodeToString(opcode: Int): String {
            return (0xE000 + opcode).toChar().toString()
        }

        /**
         * Converts a readable pattern which uses instruction mnemonics to the internal character-based format used
         * when actually attempting to find matches.
         */
        fun compile(expr: String): InstructionPattern {
            val regex = StringBuilder()
            var name = StringBuilder()

            for (c in expr) {
                if (Character.isLetterOrDigit(c) || c == '_') {
                    name.append(c)
                } else {
                    if (name.isNotEmpty()) {
                        regex.append(instructionToString(name.toString()))
                        name = StringBuilder()
                    }

                    if (!Character.isWhitespace(c)) {
                        regex.append(c)
                    }
                }
            }

            if (name.isNotEmpty()) {
                regex.append(instructionToString(name.toString()))
            }

            val pattern = Pattern.compile(regex.toString())
            return InstructionPattern(pattern)
        }

    }

}
