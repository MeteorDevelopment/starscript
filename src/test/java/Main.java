import meteordevelopment.starscript.*;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.Error;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;

public class Main {
    public static void main(String[] args) {
        String source = "Name: #1{player.name, #0, \", HI\"}";

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

        ValueMap player = new ValueMap();
        player.set("name", Value.string("MineGame159"));
        ss.set("player", Value.map(player));

        System.out.println("Input: " + source);
        System.out.println("Output: " + ss.run(script));
    }
}
