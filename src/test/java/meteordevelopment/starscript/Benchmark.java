package meteordevelopment.starscript;

import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Formatter;
import java.util.concurrent.TimeUnit;

/*
Here are the results of the benchmark below ran on my machine with JDK 17.0.5

Benchmark              Mode  Cnt  Score   Error   Units
Benchmark.format      thrpt    3  1,417 � 0,766  ops/us
Benchmark.formatter   thrpt    3  2,161 � 0,448  ops/us
Benchmark.starscript  thrpt    3  7,423 � 1,771  ops/us
Benchmark.format       avgt    3  0,707 � 0,262   us/op
Benchmark.formatter    avgt    3  0,469 � 0,273   us/op
Benchmark.starscript   avgt    3  0,141 � 0,051   us/op
 */

@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class Benchmark {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .warmupIterations(3)
                .measurementIterations(3)
                .warmupTime(TimeValue.seconds(3))
                .measurementTime(TimeValue.seconds(3))
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    public final String formatSource = "FPS: %.0f";
    public final String starscriptSource = "FPS: {round(fps)}";

    public StringBuilder sb;

    private Formatter formatter;

    public Script script;
    public Starscript ss;

    @Setup
    public void setup() throws Exception {
        sb = new StringBuilder();

        // Format
        formatter = new Formatter(sb);

        // Starscript
        script = Compiler.compile(Parser.parse(starscriptSource));

        ss = new Starscript();
        StandardLib.init(ss);
        ss.set("name", "MineGame159");
        ss.set("fps", 59.68223);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void format(Blackhole bh) {
        bh.consume(String.format(formatSource, 59.68223));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void formatter(Blackhole bh) {
        sb.setLength(0);
        bh.consume(formatter.format(formatSource, 59.68223).toString());
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void starscript(Blackhole bh) throws Exception {
        bh.consume(ss.run(script, sb).toString());
    }
}
