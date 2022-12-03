package meteordevelopment.starscript.value;

import meteordevelopment.starscript.utils.SFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/** Simpler wrapper around a map that goes from {@link String} to {@link Callable} for {@link Value}. */
public class ValueMap {
    private final Map<String, Callable<Value>> values = new HashMap<>();

    /**
     * Sets a variable supplier for the provided name. <br><br>
     *
     * <strong>Dot Notation:</strong><br>
     * If the name contains a dot it is automatically split into separate value maps. For example the name 'player.name' will not put a single string value with the name 'player.name' into this value map but another value map with the name 'player' and then inside that a string value with the name 'name'. If there already is a value named 'player' and it is a map, it just adds to that existing map, otherwise it replaces the value.
     */
    public ValueMap set(String name, Callable<Value> supplier) throws Exception {
        int dotI = name.indexOf('.');

        if (dotI >= 0) {
            // Split name based on the dot
            String name1 = name.substring(0, dotI);
            String name2 = name.substring(dotI + 1);

            // Get the map
            final ValueMap map;
            Callable<Value> valueSupplier = values.get(name1);

            if (valueSupplier == null) {
                map = new ValueMap();
                values.put(name1, () -> Value.map(map));
            }
            else {
                Value value = valueSupplier.call();

                if (value.isMap()) map = value.getMap();
                else {
                    map = new ValueMap();
                    values.put(name1, () -> Value.map(map));
                }
            }

            // Set the supplier
            map.set(name2, supplier);
        }
        else values.put(name, supplier);

        return this;
    }

    /** Sets a variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Callable)} for dot notation. */
    public ValueMap set(String name, final Value value) throws Exception {
        set(name, () -> value);
        return this;
    }

    /** Sets a boolean variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Callable)} for dot notation. */
    public ValueMap set(String name, boolean bool) throws Exception {
        return set(name, Value.bool(bool));
    }

    /** Sets a number variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Callable)} for dot notation. */
    public ValueMap set(String name, double number) throws Exception {
        return set(name, Value.number(number));
    }

    /** Sets a string variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Callable)} for dot notation. */
    public ValueMap set(String name, String string) throws Exception {
        return set(name, Value.string(string));
    }

    /** Sets a function variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Callable)} for dot notation. */
    public ValueMap set(String name, SFunction function) throws Exception {
        return set(name, Value.function(function));
    }

    /** Sets a map variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Callable)} for dot notation. */
    public ValueMap set(String name, ValueMap map) throws Exception {
        return set(name, Value.map(map));
    }

    /**
     * Gets the variable supplier for the provided name. <br><br>
     *
     * <strong>Dot Notation:</strong><br>
     * If the name is for example 'player.name' then it gets a value with the name 'player' from this map and calls .get() with 'name' on the second map. If 'player' is not a map then returns null. See {@link #set(String, Callable)}.
     */
    public Callable<Value> get(String name) throws Exception {
        int dotI = name.indexOf('.');

        if (dotI >= 0) {
            // Split name based on the dot
            String name1 = name.substring(0, dotI);
            String name2 = name.substring(dotI + 1);

            // Get child value
            Callable<Value> valueSupplier = values.get(name1);
            if (valueSupplier == null) return null;

            // Make sure the child value is a map
            Value value = valueSupplier.call();
            if (!value.isMap()) return null;

            // Get value from the child map
            return value.getMap().get(name2);
        }

        return values.get(name);
    }

    /** Gets the variable supplier for the provided name. */
    public Callable<Value> getRaw(String name) {
        return values.get(name);
    }

    /** Returns a set of all variable names. */
    public Set<String> keys() {
        return values.keySet();
    }

    /** Removes all values from this map. */
    public void clear() {
        values.clear();
    }

    /**
     * Removes a single value with the specified name from this map. <br><br>
     *
     * <strong>Dot Notation:</strong><br>
     * If the name is for example 'player.name' then it removes a value with the name 'player' from this map. See {@link #set(String, Callable)}.
     */
    public void remove(String name) {
        int dotI = name.indexOf('.');

        if (dotI >= 0) values.remove(name.substring(0, dotI));
        else values.remove(name);
    }
}
