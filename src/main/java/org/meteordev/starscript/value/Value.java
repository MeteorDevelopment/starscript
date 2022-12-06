package org.meteordev.starscript.value;

import org.meteordev.starscript.utils.SFunction;

import java.util.function.Supplier;

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
    public static Value object(java.lang.Object object) {
        return new Object(object);
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
    public boolean isObject() {
        return type == ValueType.Object;
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
    public java.lang.Object getObject() {
        return ((Object) this).object;
    }

    public boolean isTruthy() {
        switch (type) {
            default:
            case Null:     return false;
            case Boolean:  return getBool();
            case Number:
            case String:
            case Function:
            case Map:
            case Object:   return true;
        }
    }

    @Override
    public boolean equals(java.lang.Object o) {
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
            case Object:   return getObject().equals(value.getObject());
            default:       return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 31 * super.hashCode();

        switch (type) {
            case Boolean:  result += java.lang.Boolean.hashCode(getBool()); break;
            case Number:   result += Double.hashCode(getNumber()); break;
            case String:   result += getString().hashCode(); break;
            case Function: result += getFunction().hashCode(); break;
            case Map:      result += getMap().hashCode(); break;
            case Object:   result += getObject().hashCode(); break;
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
                Supplier<Value> s = getMap().getRaw("_toString");
                return s == null ? "<map>" : s.get().toString();
            }
            case Object:   return getObject().toString();
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

    private static class Object extends Value {
        private final java.lang.Object object;

        public Object(java.lang.Object object) {
            super(ValueType.Object);
            this.object = object;
        }
    }
}
