package meteordevelopment.starscript.utils;

import meteordevelopment.starscript.compiler.Expr;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Replaces usages of variables or get expressions with a different expression. <strong>Doesn't support functions.</strong><br><br>
 *
 * <strong>Examples:</strong><br>
 * addReplacer("player", () -> "foo");
 * addReplacer("player.name", () -> "foo.bar");
 */
public class VariableReplacementTransformer extends AbstractExprVisitor {
    private final Map<String, Supplier<String>> replacers = new HashMap<>();
    private final StringBuilder sb = new StringBuilder();

    public void addReplacer(String name, Supplier<String> supplier) {
        replacers.put(name, supplier);
    }

    @Override
    public void visitVariable(Expr.Variable expr) {
        tryReplace(expr, expr.name);
    }

    @Override
    public void visitGet(Expr.Get expr) {
        String name = getFullName(expr);
        if (name != null) tryReplace(expr, name);
    }

    private void tryReplace(Expr expr, String name) {
        Supplier<String> replacer = replacers.get(name);
        if (replacer == null) return;

        Expr replacement = createReplacement(replacer.get());
        expr.replace(replacement);
    }

    private Expr createReplacement(String replacement) {
        String[] parts = replacement.split("\\.");
        if (parts.length == 0) throw new IllegalStateException("Cannot replace with an empty replacement");

        Expr expr = null;

        for (int i = 0; i < parts.length; i++) {
            if (i == 0) expr = new Expr.Variable(0, 0, parts[i]);
            else expr = new Expr.Get(0, 0, expr, parts[i]);
        }

        return expr;
    }

    private String getFullName(Expr.Get expr) {
        try {
            getFullNameImpl(expr);
        }
        catch (IllegalStateException ignored) {
            sb.setLength(0);
            return null;
        }

        String name = sb.toString();
        sb.setLength(0);

        return name;
    }

    private void getFullNameImpl(Expr.Get expr) {
        if (expr.getObject() instanceof Expr.Get) getFullNameImpl((Expr.Get) expr.getObject());
        else if (expr.getObject() instanceof Expr.Variable) sb.append(((Expr.Variable) expr.getObject()).name);
        else throw new IllegalStateException();

        sb.append('.');
        sb.append(expr.name);
    }
}
