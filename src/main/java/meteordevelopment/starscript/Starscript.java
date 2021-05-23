package meteordevelopment.starscript;

import meteordevelopment.starscript.utils.Stack;
import meteordevelopment.starscript.utils.StarscriptError;
import meteordevelopment.starscript.value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** A VM (virtual machine) that can run compiled starscript code, {@link Script}. */
public class Starscript {
    private final Map<String, Supplier<Value>> globals = new HashMap<>();

    private final Stack<Value> stack = new Stack<>();

    /** Sets a variable supplier for the provided name. */
    public void set(String name, Supplier<Value> supplier) {
        globals.put(name, supplier);
    }

    /** Sets a variable supplier that always returns the same value for the provided name. */
    public void set(String name, Value value) {
        set(name, () -> value);
    }

    /** Runs the script and fills the provided {@link StringBuilder}. Throws {@link StarscriptError} if a runtime error happens. */
    public String run(Script script, StringBuilder sb) {
        stack.clear();

        sb.setLength(0);
        int ip = 0;

        loop:
        while (true) {
            switch (Instruction.valueOf(script.code[ip++])) {
                case Constant:       push(script.constants.get(script.code[ip++])); break;
                case Null:           push(Value.null_()); break;
                case True:           push(Value.bool(true)); break;
                case False:          push(Value.bool(false)); break;

                case Add:            { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(a.getNumber() + b.getNumber())); else if (a.isString()) push(Value.string(a.getString() + b.toString())); else error("Can only add 2 numbers or 1 string and other value."); break; }
                case Subtract:       { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(a.getNumber() - b.getNumber())); else error("Can only subtract 2 numbers."); break; }
                case Multiply:       { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(a.getNumber() * b.getNumber())); else error("Can only multiply 2 numbers."); break; }
                case Divide:         { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(a.getNumber() / b.getNumber())); else error("Can only divide 2 numbers."); break; }
                case Modulo:         { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(a.getNumber() % b.getNumber())); else error("Can only modulo 2 numbers."); break; }
                case Power:          { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.number(Math.pow(a.getNumber(), b.getNumber()))); else error("Can only power 2 numbers."); break; }

                case Pop:            pop(); break;
                case Not:            push(Value.bool(!pop().isTruthy())); break;
                case Negate:         { Value a = pop(); if (a.isNumber()) push(Value.number(-a.getNumber())); else error("This operation requires a number."); break; }

                case Equals:         push(Value.bool(pop().equals(pop()))); break;
                case NotEquals:      push(Value.bool(!pop().equals(pop()))); break;
                case Greater:        { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.bool(a.getNumber() > b.getNumber())); else error("This operation requires 2 number."); break; }
                case GreaterEqual:   { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.bool(a.getNumber() >= b.getNumber())); else error("This operation requires 2 number."); break; }
                case Less:           { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.bool(a.getNumber() < b.getNumber())); else error("This operation requires 2 number."); break; }
                case LessEqual:      { Value b = pop(); Value a = pop(); if (a.isNumber() && b.isNumber()) push(Value.bool(a.getNumber() <= b.getNumber())); else error("This operation requires 2 number."); break; }

                case Variable:       { String name = script.constants.get(script.code[ip++]).getString(); Supplier<Value> s = globals.get(name); if (s != null) push(s.get()); else error("Could not find variable with the name '%s'.", name); break; }
                case Get:            { String name = script.constants.get(script.code[ip++]).getString(); Supplier<Value> s = pop().getMap().get(name); if (s != null) push(s.get()); else error("Could not find field with the name '%s'.", name); break; }
                case Call:           { int argCount = script.code[ip++]; Value a = peek(argCount); if (a.isFunction()) { Value r = a.getFunction().run(this, argCount); pop(); push(r); } else error("Tried to call a %s, can only call functions.", a.type); break; }

                case Jump:           { int jump = (script.code[ip++] << 8) | script.code[ip++]; ip += jump; break; }
                case JumpIfTrue:     { int jump = (script.code[ip++] << 8) | script.code[ip++]; if (peek().isTruthy()) ip += jump; break; }
                case JumpIfFalse:     { int jump = (script.code[ip++] << 8) | script.code[ip++]; if (!peek().isTruthy()) ip += jump; break; }

                case Append:         sb.append(pop().toString()); break;
                case ConstantAppend: sb.append(script.constants.get(script.code[ip++]).toString()); break;
                case VariableAppend: { Supplier<Value> s = globals.get(script.constants.get(script.code[ip++]).getString()); sb.append((s == null ? Value.null_() : s.get()).toString()); break; }
                case GetAppend:      { String name = script.constants.get(script.code[ip++]).getString(); Supplier<Value> s = pop().getMap().get(name); if (s != null) sb.append(s.get().toString()); else error("Could not find field with the name '%s'.", name); break; }
                case CallAppend:     { int argCount = script.code[ip++]; Value a = peek(argCount); if (a.isFunction()) { Value r = a.getFunction().run(this, argCount); pop(); sb.append(r.toString()); } else error("Tried to call a %s, can only call functions.", a.type); break; }

                case End:            break loop;
            }
        }

        return sb.toString();
    }

    /** Runs the script. Throws {@link StarscriptError} if a runtime error happens. */
    public String run(Script script) {
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
        return a.isBool();
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

    // Helpers

    /** Throws a {@link StarscriptError}. */
    public void error(String format, Object... args) {
        throw new StarscriptError(String.format(format, args));
    }
}
