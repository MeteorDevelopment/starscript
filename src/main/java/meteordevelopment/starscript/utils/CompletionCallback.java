package meteordevelopment.starscript.utils;

import meteordevelopment.starscript.Starscript;

/** Used in {@link Starscript#getCompletions(String, int, CompletionCallback)}. */
public interface CompletionCallback {
    void onCompletion(String completion, boolean function);
}
