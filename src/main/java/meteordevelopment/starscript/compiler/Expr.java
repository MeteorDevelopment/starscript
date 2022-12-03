package meteordevelopment.starscript.compiler;

import java.util.List;

/** Expressions that form the AST (abstract syntax tree) of parsed starscript code. */
public abstract class Expr {
    private static final Expr[] EMPTY_CHILDREN = new Expr[0];

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
        void visitSection(Section expr);
    }

    public final int start, end;

    public Expr parent;
    public final Expr[] children;

    public Expr(int start, int end, Expr[] children) {
        this.start = start;
        this.end = end;
        this.children = children;

        for (Expr child : this.children) {
            child.parent = this;
        }
    }

    public Expr(int start, int end) {
        this(start, end, EMPTY_CHILDREN);
    }

    public abstract void accept(Visitor visitor);

    public java.lang.String getSource(java.lang.String source) {
        return source.substring(start, end);
    }

    public void replaceChild(Expr toReplace, Expr replacement) {
        for (int i = 0; i < children.length; i++) {
            if (children[i] != toReplace) continue;

            children[i] = replacement;
            toReplace.parent = null;
            replacement.parent = this;

            break;
        }
    }

    public void replace(Expr replacement) {
        parent.replaceChild(this, replacement);
    }

    public static class Null extends Expr {
        public Null(int start, int end) {
            super(start, end);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitNull(this);
        }
    }

    public static class String extends Expr {
        public final java.lang.String string;

        public String(int start, int end, java.lang.String string) {
            super(start, end);

            this.string = string;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitString(this);
        }
    }

    public static class Number extends Expr {
        public final double number;

        public Number(int start, int end, double number) {
            super(start, end);

            this.number = number;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitNumber(this);
        }
    }

    public static class Bool extends Expr {
        public final boolean bool;

        public Bool(int start, int end, boolean bool) {
            super(start, end);

            this.bool = bool;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBool(this);
        }
    }

    public static class Block extends Expr {
        public Block(int start, int end, Expr expr) {
            super(start, end, expr != null ? new Expr[] { expr } : EMPTY_CHILDREN);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBlock(this);
        }

        public Expr getExpr() {
            return children[0];
        }
    }

    public static class Group extends Expr {
        public Group(int start, int end, Expr expr) {
            super(start, end, new Expr[] { expr });
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitGroup(this);
        }

        public Expr getExpr() {
            return children[0];
        }
    }

    public static class Binary extends Expr {
        public final Token op;

        public Binary(int start, int end, Expr left, Token op, Expr right) {
            super(start, end, new Expr[] { left, right });

            this.op = op;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBinary(this);
        }

        public Expr getLeft() {
            return children[0];
        }

        public Expr getRight() {
            return children[1];
        }
    }

    public static class Unary extends Expr {
        public final Token op;

        public Unary(int start, int end, Token op, Expr right) {
            super(start, end, new Expr[] { right });

            this.op = op;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitUnary(this);
        }

        public Expr getRight() {
            return children[0];
        }
    }

    public static class Variable extends Expr {
        public final java.lang.String name;

        public Variable(int start, int end, java.lang.String name) {
            super(start, end);

            this.name = name;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitVariable(this);
        }
    }

    public static class Get extends Expr {
        public final java.lang.String name;

        public Get(int start, int end, Expr object, java.lang.String name) {
            super(start, end, new Expr[] { object });

            this.name = name;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitGet(this);
        }

        public Expr getObject() {
            return children[0];
        }
    }

    public static class Call extends Expr {
        public Call(int start, int end, Expr callee, List<Expr> args) {
            super(start, end, combine(callee, args));
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitCall(this);
        }

        public Expr getCallee() {
            return children[0];
        }

        public int getArgCount() {
            return children.length - 1;
        }

        public Expr getArg(int i) {
            return children[i + 1];
        }

        private static Expr[] combine(Expr callee, List<Expr> args) {
            Expr[] exprs = new Expr[args.size() + 1];

            exprs[0] = callee;
            for (int i = 0; i < args.size(); i++) exprs[i + 1] = args.get(i);

            return exprs;
        }
    }

    public static class Logical extends Expr {
        public final Token op;

        public Logical(int start, int end, Expr left, Token op, Expr right) {
            super(start, end, new Expr[] { left, right });

            this.op = op;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLogical(this);
        }

        public Expr getLeft() {
            return children[0];
        }

        public Expr getRight() {
            return children[1];
        }
    }

    public static class Conditional extends Expr {
        public Conditional(int start, int end, Expr condition, Expr trueExpr, Expr falseExpr) {
            super(start, end, new Expr[] { condition, trueExpr, falseExpr });
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitConditional(this);
        }

        public Expr getCondition() {
            return children[0];
        }

        public Expr getTrueExpr() {
            return children[1];
        }

        public Expr getFalseExpr() {
            return children[2];
        }
    }

    public static class Section extends Expr {
        public final int index;

        public Section(int start, int end, int index, Expr expr) {
            super(start, end, new Expr[] { expr });

            this.index = index;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitSection(this);
        }

        public Expr getExpr() {
            return children[0];
        }
    }
}
