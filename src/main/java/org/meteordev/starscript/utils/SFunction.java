package org.meteordev.starscript.utils;

import org.meteordev.starscript.Starscript;
import org.meteordev.starscript.value.Value;

/** Interface used for {@link Value#function(SFunction)}. */
public interface SFunction {
    Value run(Starscript ss, int agrCount);
}
