package meteordevelopment.starscript.compiler;

import java.util.List;

/** Expressions that form the AST (abstract syntax tree) of parsed starscript code. */
public abstract class Expr {
    public interface Visitor {
        void visitNull(Null expr);
        void visitString(String expr);
        void visitNumber(Number expr);
        void visitBool(Bool expr);
        void visitBlock(Block expr);
        void visitGroup(Group expr);
        void visitBinary(Binary expr);
        void visitUnary(Unary expr);
        void visitVariable(Variable expr);
        void visitGet(Get expr);
        void visitCall(Call expr);
        void visitLogical(Logical expr);
        void visitConditional(Conditional expr);
    }

    public abstract void accept(Visitor visitor);

    public static class Null extends Expr {
        @Override
        public void accept(Visitor visitor) {
            visitor.visitNull(this);
        }
    }

    public static class String extends Expr {
        public final java.lang.String string;

        public String(java.lang.String string) {
            this.string = string;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitString(this);
        }
    }

    public static class Number extends Expr {
        public final double number;

        public Number(double number) {
            this.number = number;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitNumber(this);
        }
    }

    public static class Bool extends Expr {
        public final boolean bool;

        public Bool(boolean bool) {
            this.bool = bool;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBool(this);
        }
    }

    public static class Block extends Expr {
        public final Expr expr;

        public Block(Expr expr) {
            this.expr = expr;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBlock(this);
        }
    }

    public static class Group extends Expr {
        public final Expr expr;

        public Group(Expr expr) {
            this.expr = expr;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitGroup(this);
        }
    }

    public static class Binary extends Expr {
        public final Expr left;
        public final Token op;
        public final Expr right;

        public Binary(Expr left, Token op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBinary(this);
        }
    }

    public static class Unary extends Expr {
        public final Token op;
        public final Expr right;

        public Unary(Token op, Expr right) {
            this.op = op;
            this.right = right;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitUnary(this);
        }
    }

    public static class Variable extends Expr {
        public final java.lang.String name;

        public Variable(java.lang.String name) {
            this.name = name;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitVariable(this);
        }
    }

    public static class Get extends Expr {
        public final Expr object;
        public final java.lang.String name;

        public Get(Expr object, java.lang.String name) {
            this.object = object;
            this.name = name;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitGet(this);
        }
    }

    public static class Call extends Expr {
        public final Expr callee;
        public final List<Expr> args;

        public Call(Expr callee, List<Expr> args) {
            this.callee = callee;
            this.args = args;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitCall(this);
        }
    }

    public static class Logical extends Expr {
        public final Expr left;
        public final Token op;
        public final Expr right;

        public Logical(Expr left, Token op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLogical(this);
        }
    }

    public static class Conditional extends Expr {
        public final Expr condition;
        public final Expr trueExpr;
        public final Expr falseExpr;

        public Conditional(Expr condition, Expr trueExpr, Expr falseExpr) {
            this.condition = condition;
            this.trueExpr = trueExpr;
            this.falseExpr = falseExpr;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitConditional(this);
        }
    }
}
