# Starscript
Fast text formatting language for Java.

- Lightweight with no dependencies
- Faster than `String.format` (See [Benchmark](https://github.com/MeteorDevelopment/starscript/blob/master/src/test/java/org/meteordev/starscript/Benchmark.java))
- Standard operators + - * / % ^
- Ability to call functions defined in java
- Variables can be different each time they are used
- Conditional output (ternary operator)
- Variables can be maps

## Examples
- `Hello {name}!`
- `Number: {someNumber * 100}`
- `FPS: {round(fps)}`
- `Today is a {good ? 'good' : 'bad'} day`
- `Name: {player.name}`

## Usage
Gradle:
```groovy
repositories {
    maven {
        name = "meteor-maven"
        url = "https://maven.meteordev.org/releases"
    }
}

dependencies {
    implementation "org.meteordev:starscript:0.2.3"
}
```

Java:
```java
// Parse
Parser.Result result = Parser.parse("Hello {name}!");

// Check for errors
if (result.hasErrors()) {
    for (Error error : result.errors) System.out.println(error);
    return;
}

// Compile
Script script = Compiler.compile(result);

// Create starscript instance
Starscript ss = new Starscript();
StandardLib.init(ss); // Adds a few default functions, not required

ss.set("name", "MineGame159");
// ss.set("name", () -> Value.string("MineGame159"));

// Run
System.out.println(ss.run(script)); // Hello MineGame159!
```

## Documentation
Full syntax and features can be found on [wiki](https://github.com/MeteorDevelopment/starscript/wiki).  
Javadocs can be found [here](https://javadoc.jitpack.io/com/github/MeteorDevelopment/starscript).
