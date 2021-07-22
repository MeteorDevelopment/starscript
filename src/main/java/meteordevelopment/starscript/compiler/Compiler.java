package meteordevelopment.starscript.compiler;

import meteordevelopment.starscript.Instruction;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.value.Value;

/** Compiler that produces compiled starscript code from {@link Parser.Result}. */
public class Compiler implements Expr.Visitor {
    private final Script script = new Script();

    private int blockDepth;

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
        script.write(blockDepth > 0 ? Instruction.Constant : Instruction.ConstantAppend, Value.string(expr.string));
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

        if (blockDepth == 1) {
            if (expr.expr instanceof Expr.Variable) variableAppend = true;
            else if (expr.expr instanceof Expr.Get) getAppend = true;
            else if (expr.expr instanceof Expr.Call) callAppend = true;
        }

        compile(expr.expr);

        if (blockDepth == 1) {
            if (!variableAppend && !getAppend && !callAppend) script.write(Instruction.Append);
            else {
                variableAppend = false;
                getAppend = false;
                callAppend = false;
            }
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
        compile(expr.right);

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

        compile(expr.object);

        getAppend = prevGetAppend;
        script.write(getAppend ? Instruction.GetAppend : Instruction.Get, Value.string(expr.name));
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

    // Helpers

    private void compile(Expr expr) {
        expr.accept(this);
    }
}
