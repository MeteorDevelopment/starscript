package org.meteordev.starscript;

import org.meteordev.starscript.compiler.Compiler;
import org.meteordev.starscript.compiler.Parser;
import org.meteordev.starscript.utils.Error;
import org.meteordev.starscript.value.Value;
import org.meteordev.starscript.value.ValueMap;

public class Main {
    private static final boolean USE_DOT_NOTATION = true;

    public static void main(String[] args) {
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
            ss.set("player.age", (ss1, agrCount) -> Value.number(5));
        }
        else {
            ss.set("player", new ValueMap()
                    .set("name", "MineGame159")
                    .set("age", (ss1, agrCount) -> Value.number(5))
            );
        }

        System.out.println("Input: " + source);
        System.out.println("Output: " + ss.run(script));
    }
}
