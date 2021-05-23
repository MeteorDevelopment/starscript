package meteordevelopment.starscript.value;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Simpler wrapper around a map that goes from {@link String} to {@link Supplier} for {@link Value}. */
public class ValueMap {
    private final Map<String, Supplier<Value>> values = new HashMap<>();

    public void set(String name, Supplier<Value> supplier) {
        values.put(name, supplier);
    }

    public void set(String name, Value value) {
        set(name, () -> value);
    }

    public Supplier<Value> get(String name) {
        return values.get(name);
    }
}
