import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.StandardLib;
import meteordevelopment.starscript.Starscript;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;

public class Benchmark {
    private static final int WARMUP = 1000000;
    private static final int NORMAL = 10000;

    public static void main(String[] args) {
        System.out.format("Warmpup iterations: %d%nNormal iterations:  %d%n%n", WARMUP, NORMAL);

        benchmarkFormat();
        benchmarkStarscript();
    }

    private static void benchmarkFormat() {
        String source = "FPS: %.0f";
        for (int i = 0; i < WARMUP; i++) String.format(source, 59.68223);

        long total = 0;

        for (int i = 0; i < NORMAL; i++) {
            long start = System.nanoTime();
            String.format(source, 59.68223);
            total += System.nanoTime() - start;
        }

        System.out.format("String.format took  %.3f milliseconds using '%s'.%n", total / 1000000.0, source);
    }

    private static void benchmarkStarscript() {
        String source = "FPS: {round(fps)}";
        Script script = Compiler.compile(Parser.parse(source));
        StringBuilder sb = new StringBuilder();

        Starscript ss = new Starscript();
        StandardLib.init(ss);
        ss.set("name", Value.string("MineGame159"));
        ss.set("fps", Value.number(59.68223));

        for (int i = 0; i < WARMUP; i++) ss.run(script, sb);

        long total = 0;

        for (int i = 0; i < NORMAL; i++) {
            long start = System.nanoTime();
            ss.run(script, sb);
            total += System.nanoTime() - start;
        }

        System.out.format("Starscript.run took %.3f milliseconds using '%s'.%n", total / 1000000.0, source);
    }
}
