package org.meteordev.starscript.utils;

import org.meteordev.starscript.Starscript;

/** Used in {@link Starscript#getCompletions(String, int, CompletionCallback)}. */
public interface CompletionCallback {
    void onCompletion(String completion, boolean function);
}
