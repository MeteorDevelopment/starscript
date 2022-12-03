package meteordevelopment.starscript;

import java.util.function.Supplier;

public class Section {
    // DNT: Java 7 Support needs this
    @SuppressWarnings("AnonymousHasLambdaAlternative")
    private static final ThreadLocal<StringBuilder> SB =
            new ThreadLocal<StringBuilder>() {
                @Override protected StringBuilder initialValue() {
                    return new StringBuilder();
                }
            };

    public final int index;
    public final String text;

    public Section next;

    public Section(int index, String text) {
        this.index = index;
        this.text = text;
    }

    @Override
    public String toString() {
        StringBuilder sb = SB.get();
        sb.setLength(0);

        Section s = this;
        while (s != null) {
            sb.append(s.text);
            s = s.next;
        }

        return sb.toString();
    }
}
