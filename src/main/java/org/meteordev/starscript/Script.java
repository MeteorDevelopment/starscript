package org.meteordev.starscript;

import org.meteordev.starscript.value.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Compiled representation of starscript code that can be run inside {@link Starscript}. */
public class Script {
    public byte[] code = new byte[8];
    private int size;

    public final List<Value> constants = new ArrayList<>();

    private void write(int b) {
        if (size >= code.length) {
            byte[] newCode = new byte[code.length * 2];
            System.arraycopy(code, 0, newCode, 0, code.length);
            code = newCode;
        }

        code[size++] = (byte) b;
    }

    /** Writes instruction to this script. */
    public void write(Instruction insn) {
        write(insn.ordinal());
    }

    /** Writes instruction with an additional byte to this script. */
    public void write(Instruction insn, int b) {
        write(insn.ordinal());
        write(b);
    }

    /** Writes instruction with an additional constant value to this script. */
    public void write(Instruction insn, Value constant) {
        write(insn.ordinal());
        writeConstant(constant);
    }

    /** Writes constant value to this script. */
    public void writeConstant(Value constant) {
        int constantI = -1;

        for (int i = 0; i < constants.size(); i++) {
            if (constants.get(i).equals(constant)) {
                constantI = i;
                break;
            }
        }

        if (constantI == -1) {
            constantI = constants.size();
            constants.add(constant);
        }

        write(constantI);
    }

    /** Begins a jump instruction. */
    public int writeJump(Instruction insn) {
        write(insn);
        write(0);
        write(0);

        return size - 2;
    }

    /** Ends a jump instruction. */
    public void patchJump(int offset) {
        int jump = size - offset - 2;

        code[offset] = (byte) ((jump >> 8) & 0xFF);
        code[offset + 1] = (byte) (jump & 0xFF);
    }

    /** Returns the number of bytes inside {@link #code}. */
    public int getSize() {
        return size;
    }

    // Decompilation

    /** Decompiles this script and writes it to the {@link Appendable} argument. */
    public void decompile(Appendable out) {
        try {
            for (int i = 0; i < size; i++) {
                Instruction insn = Instruction.valueOf(code[i]);
                out.append(String.format("%3d %-18s", i, insn));

                switch (insn) {
                    case AddConstant:
                    case Variable:
                    case VariableAppend:
                    case Get:
                    case GetAppend:
                    case Constant:
                    case ConstantAppend:    i++; out.append(String.format("%3d '%s'", code[i], constants.get(code[i] & 0xFF))); break;
                    case Call:
                    case CallAppend:        i++; out.append(String.format("%3d %s", code[i], code[i] == 1 ? "argument" : "arguments")); break;
                    case Jump:
                    case JumpIfTrue:
                    case JumpIfFalse:       i += 2; out.append(String.format("%3d -> %d", i - 2, i + 1 + (((code[i - 1] << 8) & 0xFF) | (code[i] & 0xFF)))); break;
                    case Section:           i++; out.append(String.format("%3d", code[i])); break;
                    case VariableGet:
                    case VariableGetAppend: i += 2; out.append(String.format("%3d.%-3d '%s.%s'", code[i - 1], code[i], constants.get(code[i - 1] & 0xFF), constants.get(code[i] & 0xFF))); break;
                }

                out.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Decompiles this script and writes it to {@link System#out}. */
    public void decompile() {
        decompile(System.out);
    }
}
