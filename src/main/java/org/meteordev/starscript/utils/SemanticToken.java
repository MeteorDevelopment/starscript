package org.meteordev.starscript.utils;

/** A single token containing it's {@link SemanticTokenType type} and position range where it is located in the source string. */
public class SemanticToken {
    public final SemanticTokenType type;
    public final int start, end;

    public SemanticToken(SemanticTokenType type, int start, int end) {
        this.type = type;
        this.start = start;
        this.end = end;
    }
}
