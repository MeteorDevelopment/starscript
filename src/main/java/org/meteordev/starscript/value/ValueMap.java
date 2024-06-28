package org.meteordev.starscript.value;

import org.meteordev.starscript.utils.SFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** Simpler wrapper around a map that goes from {@link String} to {@link Supplier} for {@link Value}. */
public class ValueMap {
    private final Map<String, Supplier<Value>> values = new ConcurrentHashMap<>();

    /**
     * Sets a variable supplier for the provided name. <br><br>
     *
     * <strong>Dot Notation:</strong><br>
     * If the name contains a dot it is automatically split into separate value maps. For example the name 'player.name' will not put a single string value with the name 'player.name' into this value map but another value map with the name 'player' and then inside that a string value with the name 'name'. If there already is a value named 'player' and it is a map, it just adds to that existing map, otherwise it replaces the value.
     */
    public ValueMap set(String name, Supplier<Value> supplier) {
        int dotI = name.indexOf('.');

        if (dotI >= 0) {
            // Split name based on the dot
            String name1 = name.substring(0, dotI);
            String name2 = name.substring(dotI + 1);

            // Get the map
            ValueMap map;
            Supplier<Value> valueSupplier = getRaw(name1);

            if (valueSupplier == null) {
                map = new ValueMap();
                setRaw(name1, () -> Value.map(map));
            }
            else {
                Value value = valueSupplier.get();

                if (value.isMap()) map = value.getMap();
                else {
                    map = new ValueMap();
                    setRaw(name1, () -> Value.map(map));
                }
            }

            // Set the supplier
            map.set(name2, supplier);
        }
        else setRaw(name, supplier);

        return this;
    }

    /** Sets a variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, Value value) {
        set(name, () -> value);
        return this;
    }

    /** Sets a boolean variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, boolean bool) {
        return set(name, Value.bool(bool));
    }

    /** Sets a number variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, double number) {
        return set(name, Value.number(number));
    }

    /** Sets a string variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, String string) {
        return set(name, Value.string(string));
    }

    /** Sets a function variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, SFunction function) {
        return set(name, Value.function(function));
    }

    /** Sets a map variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, ValueMap map) {
        return set(name, Value.map(map));
    }

    /** Sets an object variable supplier that always returns the same value for the provided name. <br><br> See {@link #set(String, Supplier)} for dot notation. */
    public ValueMap set(String name, Object object) {
        return set(name, Value.object(object));
    }

    /**
     * Gets the variable supplier for the provided name. <br><br>
     *
     * <strong>Dot Notation:</strong><br>
     * If the name is for example 'player.name' then it gets a value with the name 'player' from this map and calls .get() with 'name' on the second map. If 'player' is not a map then returns null. See {@link #set(String, Supplier)}.
     */
    public Supplier<Value> get(String name) {
        int dotI = name.indexOf('.');

        if (dotI >= 0) {
            // Split name based on the dot
            String name1 = name.substring(0, dotI);
            String name2 = name.substring(dotI + 1);

            // Get child value
            Supplier<Value> valueSupplier = getRaw(name1);
            if (valueSupplier == null) return null;

            // Make sure the child value is a map
            Value value = valueSupplier.get();
            if (!value.isMap()) return null;

            // Get value from the child map
            return value.getMap().get(name2);
        }

        return getRaw(name);
    }

    /** Gets the variable supplier for the provided name. */
    public Supplier<Value> getRaw(String name) {
        return values.get(name);
    }

    /** Sets the variable supplier for the provided name. */
    public Supplier<Value> setRaw(String name, Supplier<Value> supplier) {
        return values.put(name, supplier);
    }

    /** Removes the variable supplier for the provided name. */
    public Supplier<Value> removeRaw(String name) {
        return values.remove(name);
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
     * Removes a single value with the specified name from this map and returns the removed value. <br><br>
     *
     * <strong>Dot Notation:</strong><br>
     * If the name is for example 'player.name' then it attempts to get a value with the name 'player' from this map and calls .remove("name") on the second map. If `player` is not a map then the last param is removed. See {@link #set(String, Supplier)}.
     */
    public Supplier<Value> remove(String name) {
        int dotI = name.indexOf('.');

        if (dotI >= 0) {
            // Split name based on the dot
            String name1 = name.substring(0, dotI);
            String name2 = name.substring(dotI + 1);

            // Get child value
            Supplier<Value> valueSupplier = getRaw(name1);
            if (valueSupplier == null) return null;
            else {
                // Make sure the child value is a map
                Value value = valueSupplier.get();
                if (!value.isMap()) return removeRaw(name1);
                else return value.getMap().remove(name2);
            }
        }
        else return removeRaw(name);
    }
}
