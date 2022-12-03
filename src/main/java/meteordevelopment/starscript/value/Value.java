package meteordevelopment.starscript.value;

import meteordevelopment.starscript.utils.SFunction;

import java.util.concurrent.Callable;

/** Class that holds any starscript value. */
public class Value {
    private static final Value NULL = new Value(ValueType.Null);
    private static final Value TRUE = new Boolean(true);
    private static final Value FALSE = new Boolean(false);

    public final ValueType type;

    private Value(ValueType type) {
        this.type = type;
    }

    public static Value null_() {
        return NULL;
    }
    public static Value bool(boolean bool) {
        return bool ? TRUE : FALSE;
    }
    public static Value number(double number) {
        return new Number(number);
    }
    public static Value string(String string) {
        return new VString(string);
    }
    public static Value function(SFunction function) {
        return new Function(function);
    }
    public static Value map(ValueMap fields) {
        return new Map(fields);
    }

    public boolean isNull() {
        return type == ValueType.Null;
    }
    public boolean isBool() {
        return type == ValueType.Boolean;
    }
    public boolean isNumber() {
        return type == ValueType.Number;
    }
    public boolean isString() {
        return type == ValueType.String;
    }
    public boolean isFunction() {
        return type == ValueType.Function;
    }
    public boolean isMap() {
        return type == ValueType.Map;
    }

    public boolean getBool() {
        return ((Boolean) this).bool;
    }
    public double getNumber() {
        return ((Number) this).number;
    }
    public String getString() {
        return ((VString) this).string;
    }
    public SFunction getFunction() {
        return ((Function) this).function;
    }
    public ValueMap getMap() {
        return ((Map) this).fields;
    }

    public boolean isTruthy() {
        switch (type) {
            default:
            case Null:     return false;
            case Boolean:  return getBool();
            case Number:
            case String:
            case Function:
            case Map:      return true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Value value = (Value) o;
        if (type != value.type) return false;

        switch (type) {
            case Null:     return true;
            case Boolean:  return getBool() == value.getBool();
            case Number:   return getNumber() == value.getNumber();
            case String:   return getString().equals(value.getString());
            case Function: return getFunction() == value.getFunction();
            case Map:      return getMap() == value.getMap();
            default:       return false;
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        switch (type) {
            case Boolean:  result = 31 * result + (getBool() ? 1 : 0); break;
            case Number:   long temp = Double.doubleToLongBits(getNumber()); result = 31 * result + (int) (temp ^ (temp >>> 32)); break;
            case String:   String string = getString(); result = 31 * result + string.hashCode(); break;
            case Function: result = 31 * result + getFunction().hashCode(); break;
            case Map:      result = 31 * result + getMap().hashCode(); break;
        }

        return result;
    }

    @Override
    public String toString() {
        switch (type) {
            case Null:     return "null";
            case Boolean:  return getBool() ? "true" : "false";
            case Number:   { double n = getNumber(); return n % 1 == 0 ? Integer.toString((int) n) : Double.toString(n); }
            case String:   return getString();
            case Function: return "<function>";
            case Map: {
                Callable<Value> s = getMap().getRaw("_toString");
                try {
                    return s == null ? "<map>" : s.call().toString();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            default:       return "";
        }
    }

    private static class Boolean extends Value {
        private final boolean bool;

        private Boolean(boolean bool) {
            super(ValueType.Boolean);
            this.bool = bool;
        }
    }

    private static class Number extends Value {
        private final double number;

        private Number(double number) {
            super(ValueType.Number);
            this.number = number;
        }
    }

    private static class VString extends Value {
        private final String string;

        private VString(String string) {
            super(ValueType.String);
            this.string = string;
        }
    }

    private static class Function extends Value {
        private final SFunction function;

        public Function(SFunction function) {
            super(ValueType.Function);
            this.function = function;
        }
    }

    private static class Map extends Value {
        private final ValueMap fields;

        public Map(ValueMap fields) {
            super(ValueType.Map);
            this.fields = fields;
        }
    }
}
