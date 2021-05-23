package meteordevelopment.starscript;

/** Instructions used by {@link Starscript}. */
public enum Instruction {
    Constant,
    Null,
    True,
    False,

    Add,
    Subtract,
    Multiply,
    Divide,
    Modulo,
    Power,

    Pop,
    Not,
    Negate,

    Equals,
    NotEquals,
    Greater,
    GreaterEqual,
    Less,
    LessEqual,

    Variable,
    Get,
    Call,

    Jump,
    JumpIfTrue,
    JumpIfFalse,

    Append,
    ConstantAppend,
    VariableAppend,
    GetAppend,
    CallAppend,

    End;

    private static final Instruction[] values = values();

    public static Instruction valueOf(int i) {
        return values[i];
    }
}
