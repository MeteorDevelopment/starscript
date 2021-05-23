package meteordevelopment.starscript;

import meteordevelopment.starscript.value.Value;

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

        write(insn.ordinal());
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

        code[offset] = (byte) ((jump >> 8) & 0xff);
        code[offset + 1] = (byte) (jump & 0xff);
    }

    // Decompilation

    /** Decompiles this script and writes it to {@link System#out}. */
    public void decompile() {
        for (int i = 0; i < size; i++) {
            Instruction insn = Instruction.valueOf(code[i]);
            System.out.format("%3d %-16s", i, insn);

            switch (insn) {
                case Variable:
                case VariableAppend:
                case Get:
                case GetAppend:
                case Constant:
                case ConstantAppend: i++; System.out.format("%3d '%s'", code[i], constants.get(code[i])); break;
                case Call:
                case CallAppend:     i++; System.out.format("%3d %s", code[i], code[i] == 1 ? "argument" : "arguments"); break;
                case Jump:
                case JumpIfTrue:
                case JumpIfFalse:    i += 2; System.out.format("%3d -> %d", i - 2, i + 1 + ((code[i - 1] << 8) | code[i])); break;
            }

            System.out.println();
        }
    }
}
