package meteordevelopment.starscript.compiler;

import meteordevelopment.starscript.Instruction;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.value.Value;

/** Compiler that produces compiled starscript code from {@link Parser.Result}. */
public class Compiler implements Expr.Visitor {
    private final Script script = new Script();

    private int blockDepth;

    private boolean constantAppend;
    private boolean variableAppend;
    private boolean getAppend;
    private boolean callAppend;

    private Compiler() {}

    /** Produces compiled {@link Script} from {@link Parser.Result} that can be run inside {@link meteordevelopment.starscript.Starscript}. */
    public static Script compile(Parser.Result result) {
        Compiler compiler = new Compiler();

        for (Expr expr : result.exprs) compiler.compile(expr);
        compiler.script.write(Instruction.End);

        return compiler.script;
    }

    // Expressions

    @Override
    public void visitNull(Expr.Null expr) {
        script.write(Instruction.Null);
    }

    @Override
    public void visitString(Expr.String expr) {
        script.write((blockDepth == 0 || constantAppend) ? Instruction.ConstantAppend : Instruction.Constant, Value.string(expr.string));
    }

    @Override
    public void visitNumber(Expr.Number expr) {
        script.write(Instruction.Constant, Value.number(expr.number));
    }

    @Override
    public void visitBool(Expr.Bool expr) {
        script.write(expr.bool ? Instruction.True : Instruction.False);
    }

    @Override
    public void visitBlock(Expr.Block expr) {
        blockDepth++;

        if (expr.expr instanceof Expr.String) constantAppend = true;
        else if (expr.expr instanceof Expr.Variable) variableAppend = true;
        else if (expr.expr instanceof Expr.Get) getAppend = true;
        else if (expr.expr instanceof Expr.Call) callAppend = true;

        compile(expr.expr);

        if (!constantAppend && !variableAppend && !getAppend && !callAppend) script.write(Instruction.Append);
        else {
            constantAppend = false;
            variableAppend = false;
            getAppend = false;
            callAppend = false;
        }

        blockDepth--;
    }

    @Override
    public void visitGroup(Expr.Group expr) {
        compile(expr.expr);
    }

    @Override
    public void visitBinary(Expr.Binary expr) {
        compile(expr.left);

        if (expr.op == Token.Plus && (expr.right instanceof Expr.String || expr.right instanceof Expr.Number)) {
            script.write(Instruction.AddConstant, expr.right instanceof Expr.String ? Value.string(((Expr.String) expr.right).string) : Value.number(((Expr.Number) expr.right).number));
            return;
        }
        else compile(expr.right);

        switch (expr.op) {
            case Plus:         script.write(Instruction.Add); break;
            case Minus:        script.write(Instruction.Subtract); break;
            case Star:         script.write(Instruction.Multiply); break;
            case Slash:        script.write(Instruction.Divide); break;
            case Percentage:   script.write(Instruction.Modulo); break;
            case UpArrow:      script.write(Instruction.Power); break;

            case EqualEqual:   script.write(Instruction.Equals); break;
            case BangEqual:    script.write(Instruction.NotEquals); break;
            case Greater:      script.write(Instruction.Greater); break;
            case GreaterEqual: script.write(Instruction.GreaterEqual); break;
            case Less:         script.write(Instruction.Less); break;
            case LessEqual:    script.write(Instruction.LessEqual); break;
        }
    }

    @Override
    public void visitUnary(Expr.Unary expr) {
        compile(expr.right);

        if (expr.op == Token.Bang) script.write(Instruction.Not);
        else if (expr.op == Token.Minus) script.write(Instruction.Negate);
    }

    @Override
    public void visitVariable(Expr.Variable expr) {
        script.write(variableAppend ? Instruction.VariableAppend : Instruction.Variable, Value.string(expr.name));
    }

    @Override
    public void visitGet(Expr.Get expr) {
        boolean prevGetAppend = getAppend;
        getAppend = false;

        boolean variableGet = expr.object instanceof Expr.Variable;
        if (!variableGet) compile(expr.object);

        getAppend = prevGetAppend;

        if (variableGet) {
            script.write(getAppend ? Instruction.VariableGetAppend : Instruction.VariableGet, Value.string(((Expr.Variable) expr.object).name));
            script.writeConstant(Value.string(expr.name));
        }
        else script.write(getAppend ? Instruction.GetAppend : Instruction.Get, Value.string(expr.name));
    }

    @Override
    public void visitCall(Expr.Call expr) {
        boolean prevCallAppend = callAppend;
        compile(expr.callee);

        callAppend = false;
        for (Expr e : expr.args) compile(e);

        callAppend = prevCallAppend;
        script.write(callAppend ? Instruction.CallAppend : Instruction.Call, expr.args.size());
    }

    @Override
    public void visitLogical(Expr.Logical expr) {
        compile(expr.left);
        int endJump = script.writeJump(expr.op == Token.And ? Instruction.JumpIfFalse : Instruction.JumpIfTrue);

        script.write(Instruction.Pop);
        compile(expr.right);

        script.patchJump(endJump);
    }

    @Override
    public void visitConditional(Expr.Conditional expr) {
        compile(expr.condition);
        int falseJump = script.writeJump(Instruction.JumpIfFalse);

        script.write(Instruction.Pop);
        compile(expr.trueExpr);
        int endJump = script.writeJump(Instruction.Jump);

        script.patchJump(falseJump);
        script.write(Instruction.Pop);
        compile(expr.falseExpr);

        script.patchJump(endJump);
    }

    @Override
    public void visitSection(Expr.Section expr) {
        script.write(Instruction.Section, expr.index);
        compile(expr.expr);
    }

    // Helpers

    private void compile(Expr expr) {
        if (expr != null) expr.accept(this);
    }
}
