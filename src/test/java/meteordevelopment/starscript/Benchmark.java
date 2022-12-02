package meteordevelopment.starscript;

import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import org.openjdk.jmh.annotations.*;
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

    @org.openjdk.jmh.annotations.Benchmark
    public String format() {
        String source = "FPS: %.0f";
        String result = "";

        for (int i = 0; i < iterations; i++) {
            result = String.format(source, 59.68223);
        }

        return result;
    }

    @org.openjdk.jmh.annotations.Benchmark
    public String starscript() {
        String source = "FPS: {round(fps)}";
        Script script = Compiler.compile(Parser.parse(source));
        StringBuilder sb = new StringBuilder();

        Starscript ss = new Starscript();
        StandardLib.init(ss);
        ss.set("name", "MineGame159");
        ss.set("fps", 59.68223);

        String result = "";

        for (int i = 0; i < iterations; i++) {
            result = ss.run(script, sb).toString();
        }

        return result;
    }
}
