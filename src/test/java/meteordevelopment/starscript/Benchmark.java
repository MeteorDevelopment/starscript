package meteordevelopment.starscript;

import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class Benchmark {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*")
                .warmupIterations(3)
                .measurementIterations(3)
                .jvmArgs("-Xms2G", "-Xmx2G")
                .shouldDoGC(true)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Param({"10000"})
    public int iterations;

    public final String formatSource = "FPS: %.0f";
    public final String starscriptSource = "FPS: {round(fps)}";

    public Script script;
    public StringBuilder sb;
    public Starscript ss;

    @Setup
    public void setup() {
        script = Compiler.compile(Parser.parse(starscriptSource));
        sb = new StringBuilder();

        ss = new Starscript();
        StandardLib.init(ss);
        ss.set("name", "MineGame159");
        ss.set("fps", 59.68223);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void format(Blackhole bh) {
        for (int i = 0; i < iterations; i++) {
            bh.consume(String.format(formatSource, 59.68223));
        }
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void starscript(Blackhole bh) {
        for (int i = 0; i < iterations; i++) {
            bh.consume(ss.run(script, sb).toString());
        }
    }
}
