package org.meteordev.starscript.value;

import org.meteordev.starscript.utils.SFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** Simpler wrapper around a map that goes from {@link String} to {@link Supplier} for {@link Value}. */
public class ValueMap {
    private final Map<String, Supplier<Value>> values = new HashMap<>();

    /**
     * Sets a variable supplier for the provided name. <br><br>
     *
     * <strong>Dot Notation:</strong><br>
     * If the name contains a dot it is automatically split into separate value maps. For example the name 'player.name' will not put a single string value with the name 'player.name' into this value map but another value map with the name 'player' and then inside that a string value with the name 'name'. If there already is a value named 'player' and it is a map, it just adds to that existing map, otherwise it replaces the value.
     */
    public ValueMap set(String name, Supplier<Value> supplier) {
        // Check if the name contains a dot
        int dotIndex = name.indexOf('.');

        if (dotIndex >= 0) {
            // If it contains a dot, split the name into two parts
            String parentName = name.substring(0, dotIndex);
            String childName = name.substring(dotIndex + 1);

            // Get the map or create a new one if it doesn't exist
            ValueMap map;
            Supplier<Value> parentSupplier = values.get(parentName);

            if (parentSupplier == null) {
                map = new ValueMap();
                values.put(parentName, () -> Value.map(map));
            } else {
                Value parentValue = parentSupplier.get();

                if (parentValue.isMap()) {
                    map = parentValue.getMap();
                } else {
                    map = new ValueMap();
                    values.put(parentName, () -> Value.map(map));
                }
            }

            // Set the supplier for the child value
            map.set(childName, supplier);
        } else {
            // If there's no dot in the name, directly set the supplier
            values.put(name, supplier);
        }

        // Return the updated ValueMap
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
        // Check if the name contains a dot
        int dotIndex = name.indexOf('.');

        if (dotIndex >= 0) {
            // If it contains a dot, split the name into two parts
            String parentName = name.substring(0, dotIndex);
            String childName = name.substring(dotIndex + 1);

            // Get the Supplier for the parent value
            Supplier<Value> parentSupplier = values.get(parentName);

            // If the parent Supplier is not found, return null
            if (parentSupplier == null) {
                return null;
            }

            // Get the actual Value from the parent Supplier
            Value parentValue = parentSupplier.get();

            // Check if the parent Value is a map
            if (!parentValue.isMap()) {
                return null;
            }

            // Get the child Value from the parent Map
            return parentValue.getMap().get(childName);
        } else {
            // If there's no dot in the name, directly get the Supplier from the map
            return values.get(name);
        }
    }


    /** Gets the variable supplier for the provided name. */
    public Supplier<Value> getRaw(String name) {
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
     * Removes a single value with the specified name from this map and returns the removed value. <br><br>
     *
     * <strong>Dot Notation:</strong><br>
     * If the name is for example 'player.name' then it attempts to get a value with the name 'player' from this map and calls .remove("name") on the second map. If `player` is not a map then the last param is removed. See {@link #set(String, Supplier)}.
     */
    public Supplier<Value> remove(String name) {
        // Check if the name contains a dot
        int dotIndex = name.indexOf('.');

        if (dotIndex >= 0) {
            // If it contains a dot, split the name into two parts
            String parentName = name.substring(0, dotIndex);
            String childName = name.substring(dotIndex + 1);

            // Get the Supplier for the parent value
            Supplier<Value> parentSupplier = values.get(parentName);

            if (parentSupplier == null) {
                // If the parent Supplier is not found, return null
                return null;
            } else {
                // Get the actual Value from the parent Supplier
                Value parentValue = parentSupplier.get();

                if (!parentValue.isMap()) {
                    // If the parent Value is not a map, remove the parent entry
                    return values.remove(parentName);
                } else {
                    // Get the child Value from the parent Map
                    Supplier<Value> childSupplier = parentValue.getMap().remove(childName);
                    if (childSupplier != null) {
                        return childSupplier;
                    } else {
                        // If the child Value is not found, return null
                        return null;
                    }
                }
            }
        } else {
            // If there's no dot in the name, directly remove the entry
            return values.remove(name);
        }
    }

}
