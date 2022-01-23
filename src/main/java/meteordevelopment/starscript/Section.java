package meteordevelopment.starscript;

public class Section {
    private static final ThreadLocal<StringBuilder> SB = ThreadLocal.withInitial(StringBuilder::new);

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
