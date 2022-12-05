package org.meteordev.starscript.utils;

import org.meteordev.starscript.compiler.Expr;

public abstract class AbstractExprVisitor implements Expr.Visitor {
    @Override
    public void visitNull(Expr.Null expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitString(Expr.String expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitNumber(Expr.Number expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitBool(Expr.Bool expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitBlock(Expr.Block expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitGroup(Expr.Group expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitBinary(Expr.Binary expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitUnary(Expr.Unary expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitVariable(Expr.Variable expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitGet(Expr.Get expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitCall(Expr.Call expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitLogical(Expr.Logical expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitConditional(Expr.Conditional expr) {
        for (Expr child : expr.children) child.accept(this);
    }

    @Override
    public void visitSection(Expr.Section expr) {
        for (Expr child : expr.children) child.accept(this);
    }
}
