package org.meteordev.starscript;

import org.meteordev.starscript.compiler.Expr;
import org.meteordev.starscript.compiler.Parser;
import org.meteordev.starscript.utils.Error;
import org.meteordev.starscript.utils.*;
import org.meteordev.starscript.value.Value;
import org.meteordev.starscript.value.ValueMap;

import java.util.function.Supplier;

/** A VM (virtual machine) that can run compiled starscript code, {@link Script}. */
public class Starscript {
    private final ValueMap globals;

    private final Stack<Value> stack = new Stack<>();

    public Starscript() {
        globals = new ValueMap();
    }

    /** Creates a new Starscript instance with shared globals ({@link #getGlobals()}) from the parent instance. */
    public Starscript(Starscript parent) {
        globals = parent.globals;
    }

    /** Runs the script and fills the provided {@link StringBuilder}. Throws {@link StarscriptError} if a runtime error happens. */
    public Section run(Script script, StringBuilder sb) {
        stack.clear();

        sb.setLength(0);
        int ip = 0;

        Section firstSection = null;
        Section section = null;
        int index = 0;

        loop:
        while (true) {
            switch (Instruction.valueOf(script.code[ip++])) {
                case Constant:          push(script.constants.get(script.code[ip++])); break;
                case Null:              push(Value.null_()); break;
                case True:              push(Value.bool(true)); break;
                case False:             push(Value.bool(false)); break;

                case Add:               { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(a.getNumber() + b.getNumber())); else if (a.isString()) push(Value.string(a.getString() + b.toString())); else error("Can only add 2 numbers or 1 string and other value."); break; }
                case Subtract:          { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(a.getNumber() - b.getNumber())); else error("Can only subtract 2 numbers."); break; }
                case Multiply:          { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(a.getNumber() * b.getNumber())); else error("Can only multiply 2 numbers."); break; }
                case Divide:            { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(a.getNumber() / b.getNumber())); else error("Can only divide 2 numbers."); break; }
                case Modulo:            { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(a.getNumber() % b.getNumber())); else error("Can only modulo 2 numbers."); break; }
                case Power:             { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(Math.pow(a.getNumber(), b.getNumber()))); else error("Can only power 2 numbers."); break; }

                case AddConstant:       { Value b = script.constants.get(script.code[ip++]); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(a.getNumber() + b.getNumber())); else if (a.isString()) push(Value.string(a.getString() + b.toString())); else error("Can only add 2 numbers or 1 string and other value."); break; }

                case Pop:               pop(); break;
                case Not:               push(Value.bool(!pop().isTruthy())); break;
                case Negate:            { Value a = pop(); if (a.isNumber()) push(Value.number(-a.getNumber())); else error("This operation requires a number."); break; }

                case Equals:            push(Value.bool(pop().equals(pop()))); break;
                case NotEquals:         push(Value.bool(!pop().equals(pop()))); break;
                case Greater:           { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.bool(a.getNumber() > b.getNumber())); else error("This operation requires 2 number."); break; }
                case GreaterEqual:      { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.bool(a.getNumber() >= b.getNumber())); else error("This operation requires 2 number."); break; }
                case Less:              { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.bool(a.getNumber() < b.getNumber())); else error("This operation requires 2 number."); break; }
                case LessEqual:         { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.bool(a.getNumber() <= b.getNumber())); else error("This operation requires 2 number."); break; }

                case Variable:          { String name = script.constants.get(script.code[ip++]).getString(); Supplier<Value> s = globals.getRaw(name); push(s != null ? s.get() : Value.null_()); break; }
                case Get:               { String name = script.constants.get(script.code[ip++]).getString(); Value v = pop(); if (!v.isMap()) { push(Value.null_()); break; } Supplier<Value> s = v.getMap().getRaw(name); push(s != null ? s.get() : Value.null_()); break; }
                case Call:              { int argCount = script.code[ip++]; Value a = peek(argCount); if (a.isFunction()) { Value r = a.getFunction().run(this, argCount); pop(); push(r); } else error("Tried to call a %s, can only call functions.", a.type); break; }

                case Jump:              { int jump = ((script.code[ip++] << 8) & 0xFF) | (script.code[ip++] & 0xFF); ip += jump; break; }
                case JumpIfTrue:        { int jump = ((script.code[ip++] << 8) & 0xFF) | (script.code[ip++] & 0xFF); if (peek().isTruthy()) ip += jump; break; }
                case JumpIfFalse:       { int jump = ((script.code[ip++] << 8) & 0xFF) | (script.code[ip++] & 0xFF); if (!peek().isTruthy()) ip += jump; break; }

                case Section:           if (firstSection == null) { firstSection = new Section(index, sb.toString()); section = firstSection; } else { section.next = new Section(index, sb.toString()); section = section.next; } sb.setLength(0); index = script.code[ip++]; break;

                case Append:            sb.append(pop().toString()); break;
                case ConstantAppend:    sb.append(script.constants.get(script.code[ip++]).toString()); break;
                case VariableAppend:    { Supplier<Value> s = globals.getRaw(script.constants.get(script.code[ip++]).getString()); sb.append((s == null ? Value.null_() : s.get()).toString()); break; }
                case GetAppend:         { String name = script.constants.get(script.code[ip++]).getString(); Value v = pop(); if (!v.isMap()) { sb.append(Value.null_()); break; } Supplier<Value> s = v.getMap().getRaw(name); sb.append((s != null ? s.get() : Value.null_()).toString()); break; }
                case CallAppend:        { int argCount = script.code[ip++]; Value a = peek(argCount); if (a.isFunction()) { Value r = a.getFunction().run(this, argCount); pop(); sb.append(r.toString()); } else error("Tried to call a %s, can only call functions.", a.type); break; }

                case VariableGet:       {
                    Value v;
                    { String name = script.constants.get(script.code[ip++]).getString(); Supplier<Value> s = globals.getRaw(name); v = s != null ? s.get() : Value.null_(); } // Variable
                    { String name = script.constants.get(script.code[ip++]).getString(); if (!v.isMap()) { push(Value.null_()); break; } Supplier<Value> s = v.getMap().getRaw(name); push(s != null ? s.get() : Value.null_()); } // Get
                    break;
                }
                case VariableGetAppend: {
                    Value v;
                    { String name = script.constants.get(script.code[ip++]).getString(); Supplier<Value> s = globals.getRaw(name); v = s != null ? s.get() : Value.null_(); } // Variable
                    { String name = script.constants.get(script.code[ip++]).getString(); if (!v.isMap()) { push(Value.null_()); break; } Supplier<Value> s = v.getMap().getRaw(name); v = s != null ? s.get() : Value.null_(); } // Get
                    { sb.append(v.toString()); } // Append
                    break;
                }

                case End:               break loop;
                default:                throw new UnsupportedOperationException("Unknown instruction '" + Instruction.valueOf(script.code[ip]) + "'");
            }
        }

        if (firstSection != null) {
            section.next = new Section(index, sb.toString());
            return firstSection;
        }

        return new Section(index, sb.toString());
    }

    /** Runs the script. Throws {@link StarscriptError} if a runtime error happens. */
    public Section run(Script script) {
        return run(script, new StringBuilder());
    }

    // Stack manipulation

    /** Pushes a new value on the stack. */
    public void push(Value value) {
        stack.push(value);
    }

    /** Pops a new value from the stack. */
    public Value pop() {
        return stack.pop();
    }

    /** Returns a value from the stack without removing it. */
    public Value peek() {
        return stack.peek();
    }

    /** Returns a value from the stack with an offset without removing it. */
    public Value peek(int offset) {
        return stack.peek(offset);
    }

    /** Pops a value from the stack and returns it as boolean. Calls {@link Starscript#error(String, Object...)} with the provided message if the value is not boolean. */
    public boolean popBool(String errorMsg) {
        Value a = pop();
        if (!a.isBool()) error(errorMsg);
        return a.getBool();
    }

    /** Pops a value from the stack and returns it as double. Calls {@link Starscript#error(String, Object...)} with the provided message if the value is not double. */
    public double popNumber(String errorMsg) {
        Value a = pop();
        if (!a.isNumber()) error(errorMsg);
        return a.getNumber();
    }

    /** Pops a value from the stack and returns it as String. Calls {@link Starscript#error(String, Object...)} with the provided message if the value is not String. */
    public String popString(String errorMsg) {
        Value a = pop();
        if (!a.isString()) error(errorMsg);
        return a.getString();
    }

    /** Pops a value from the stack and returns it as Object. Calls {@link Starscript#error(String, Object...)} with the provided message if the value is not Object. */
    public Object popObject(String errorMsg) {
        Value a = pop();
        if (!a.isObject()) error(errorMsg);
        return a.getObject();
    }

    // Helpers

    /** Throws a {@link StarscriptError}. */
    public void error(String format, Object... args) {
        throw new StarscriptError(String.format(format, args));
    }

    // Globals

    /** Sets a variable supplier for the provided name. <br><br> See {@link ValueMap#set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, Supplier<Value> supplier) {
        return globals.set(name, supplier);
    }

    /** Sets a variable supplier that always returns the same value for the provided name. <br><br> See {@link ValueMap#set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, Value value) {
        return globals.set(name, value);
    }

    /** Sets a boolean variable supplier that always returns the same value for the provided name. <br><br> See {@link ValueMap#set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, boolean bool) {
        return globals.set(name, bool);
    }

    /** Sets a number variable supplier that always returns the same value for the provided name. <br><br> See {@link ValueMap#set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, double number) {
        return globals.set(name, number);
    }

    /** Sets a string variable supplier that always returns the same value for the provided name. <br><br> See {@link ValueMap#set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, String string) {
        return globals.set(name, string);
    }

    /** Sets a function variable supplier that always returns the same value for the provided name. <br><br> See {@link ValueMap#set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, SFunction function) {
        return globals.set(name, function);
    }

    /** Sets a map variable supplier that always returns the same value for the provided name. <br><br> See {@link ValueMap#set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, ValueMap map) {
        return globals.set(name, map);
    }

    /** Sets an object variable supplier that always returns the same value for the provided name. <br><br> See {@link ValueMap#set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, Object object) {
        return globals.set(name, object);
    }

    /** Removes all values from the globals. */
    public void clear() {
        globals.clear();
    }

    /** Removes a single value with the specified name from the globals and returns the removed value. <br><br> See {@link ValueMap#remove(String)} for dot notation. */
    public Supplier<Value> remove(String name) {
        return globals.remove(name);
    }

    /** Returns the underlying {@link ValueMap} for global variables. */
    public ValueMap getGlobals() {
        return globals;
    }

    // Completions

    /** Calls the provided callback for every completion that is able to be resolved from global variables. */
    public void getCompletions(String source, int position, CompletionCallback callback) {
        Parser.Result result = Parser.parse(source);

        for (Expr expr : result.exprs) {
            completionsExpr(source, position, expr, callback);
        }

        for (Error error : result.errors) {
            if (error.expr != null) completionsExpr(source, position, error.expr, callback);
        }
    }

    private void completionsExpr(String source, int position, Expr expr, CompletionCallback callback) {
        if (position < expr.start || (position > expr.end && position != source.length())) return;

        if (expr instanceof Expr.Variable) {
            Expr.Variable var = (Expr.Variable) expr;
            String start = source.substring(var.start, position);

            for (String key : globals.keys()) {
                if (!key.startsWith("_") && key.startsWith(start)) callback.onCompletion(key, globals.getRaw(key).get().isFunction());
            }
        }
        else if (expr instanceof Expr.Get) {
            Expr.Get get = (Expr.Get) expr;

            if (position >= get.end - get.name.length()) {
                Value value = resolveExpr(get.getObject());

                if (value != null && value.isMap()) {
                    String start = source.substring(get.getObject().end + 1, position);

                    for (String key : value.getMap().keys()) {
                        if (!key.startsWith("_") && key.startsWith(start)) callback.onCompletion(key, value.getMap().getRaw(key).get().isFunction());
                    }
                }
            }
            else {
                for (Expr child : expr.children) completionsExpr(source, position, child, callback);
            }
        }
        else if (expr instanceof Expr.Block) {
            if (((Expr.Block) expr).getExpr() == null) {
                for (String key : globals.keys()) {
                    if (!key.startsWith("_")) callback.onCompletion(key, globals.getRaw(key).get().isFunction());
                }
            }
            else {
                for (Expr child : expr.children) completionsExpr(source, position, child, callback);
            }
        }
        else {
            for (Expr child : expr.children) completionsExpr(source, position, child, callback);
        }
    }

    private Value resolveExpr(Expr expr) {
        if (expr instanceof Expr.Variable) {
            Supplier<Value> supplier = globals.getRaw(((Expr.Variable) expr).name);
            return supplier != null ? supplier.get() : null;
        }
        else if (expr instanceof Expr.Get) {
            Value value = resolveExpr(((Expr.Get) expr).getObject());
            if (value == null || !value.isMap()) return null;

            Supplier<Value> supplier = value.getMap().getRaw(((Expr.Get) expr).name);
            return supplier != null ? supplier.get() : null;
        }

        return null;
    }
}
