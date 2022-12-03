package meteordevelopment.starscript.utils;

import meteordevelopment.starscript.compiler.Expr;

public abstract class AbstractExprVisitor implements Expr.Visitor {
    @Override
    public void visitNull(Expr.Null expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitString(Expr.String expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitNumber(Expr.Number expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitBool(Expr.Bool expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitBlock(Expr.Block expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitGroup(Expr.Group expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitBinary(Expr.Binary expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitUnary(Expr.Unary expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitVariable(Expr.Variable expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitGet(Expr.Get expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitCall(Expr.Call expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitLogical(Expr.Logical expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitConditional(Expr.Conditional expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitSection(Expr.Section expr) throws Exception {
        for (Expr child : expr.children) child.accept(this);
    }
}
