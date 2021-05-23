package meteordevelopment.starscript.utils;

/** Class for storing errors produced while parsing. */
public class Error {
    public final int line;
    public final int character;
    public final char ch;
    public final String message;

    public Error(int line, int character, char ch, String message) {
        this.line = line;
        this.character = character;
        this.ch = ch;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("[line %d, character %d] at '%s': %s", line, character, ch, message);
    }
}
