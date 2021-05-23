package meteordevelopment.starscript.utils;

import meteordevelopment.starscript.Starscript;
import meteordevelopment.starscript.value.Value;

/** Interface used for {@link Value#function(SFunction)}. */
public interface SFunction {
    Value run(Starscript ss, int agrCount);
}
