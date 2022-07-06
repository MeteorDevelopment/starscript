package meteordevelopment.starscript.value;

import meteordevelopment.starscript.utils.SFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** Simpler wrapper around a map that goes from {@link String} to {@link Supplier} for {@link Value}. */
public class ValueMap {
    private final Map<String, Supplier<Value>> values = new HashMap<>();

    /** Sets a variable supplier for the provided name. */
    public ValueMap set(String name, Supplier<Value> supplier) {
        values.put(name, supplier);
        return this;
    }

    /** Sets a variable supplier that always returns the same value for the provided name. */
    public ValueMap set(String name, Value value) {
        set(name, () -> value);
        return this;
    }

    /** Sets a boolean variable supplier that always returns the same value for the provided name. */
    public ValueMap set(String name, boolean bool) {
        return set(name, Value.bool(bool));
    }

    /** Sets a number variable supplier that always returns the same value for the provided name. */
    public ValueMap set(String name, double number) {
        return set(name, Value.number(number));
    }

    /** Sets a string variable supplier that always returns the same value for the provided name. */
    public ValueMap set(String name, String string) {
        return set(name, Value.string(string));
    }

    /** Sets a function variable supplier that always returns the same value for the provided name. */
    public ValueMap set(String name, SFunction function) {
        return set(name, Value.function(function));
    }

    /** Sets a map variable supplier that always returns the same value for the provided name. */
    public ValueMap set(String name, ValueMap map) {
        return set(name, Value.map(map));
    }

    /** Gets the variable supplier for the provided name. */
    public Supplier<Value> get(String name) {
        return values.get(name);
    }

    /** Returns a set of all variable names. */
    public Set<String> keys() {
        return values.keySet();
    }
}
