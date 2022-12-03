package meteordevelopment.starscript;

import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.Error;
import meteordevelopment.starscript.utils.SFunction;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;

public class Main {
    private static final boolean USE_DOT_NOTATION = true;

    public static void main(String[] args) throws Exception {
        String source = "Name: {player.name}     Age: {player.age()}";

        Parser.Result result = Parser.parse(source);
        Script script = Compiler.compile(result);

        script.decompile();
        System.out.println();

        if (result.hasErrors()) {
            for (Error error : result.errors) System.out.println(error);
            System.out.println();
        }

        Starscript ss = new Starscript();
        StandardLib.init(ss);

        if (USE_DOT_NOTATION) {
            ss.set("player.name", "MineGame159");
            ss.set("player.age", new SFunction() {
                @Override
                public Value run(Starscript ss1, int agrCount) {
                    return Value.number(5);
                }
            });
        }
        else {
            ss.set("player", new ValueMap()
                    .set("name", "MineGame159")
                    .set("age", new SFunction() {
                        @Override
                        public Value run(Starscript ss1, int agrCount) {
                            return Value.number(5);
                        }
                    })
            );
        }

        System.out.println("Input: " + source);
        System.out.println("Output: " + ss.run(script));
    }
}
