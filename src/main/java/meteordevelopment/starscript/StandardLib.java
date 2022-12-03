package meteordevelopment.starscript;

import meteordevelopment.starscript.utils.SFunction;
import meteordevelopment.starscript.value.Value;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;

/** Standard library with some default functions and variables. */
public class StandardLib {
    private static final Random rand = new Random();

    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd. MM. yyyy");

    /** Adds the functions and variables to the provided {@link Starscript} instance. */
    public static void init(Starscript ss) throws Exception {
        // Variables
        ss.set("PI", Math.PI);
        ss.set("time", new Callable<Value>() {
            @Override
            public Value call() {
                return Value.string(timeFormat.format(new Date()));
            }
        });
        ss.set("date", new Callable<Value>() {
            @Override
            public Value call() {
                return Value.string(dateFormat.format(new Date()));
            }
        });

        // Numbers
        ss.set("round", new SFunction() {
            @Override
            public Value run(Starscript ss12, int argCount11) {
                return round(ss12, argCount11);
            }
        });
        ss.set("roundToString", new SFunction() {
            @Override
            public Value run(Starscript ss11, int argCount10) {
                return roundToString(ss11, argCount10);
            }
        });
        ss.set("floor", new SFunction() {
            @Override
            public Value run(Starscript ss10, int argCount9) {
                return floor(ss10, argCount9);
            }
        });
        ss.set("ceil", new SFunction() {
            @Override
            public Value run(Starscript ss9, int argCount8) {
                return ceil(ss9, argCount8);
            }
        });
        ss.set("abs", new SFunction() {
            @Override
            public Value run(Starscript ss8, int argCount7) {
                return abs(ss8, argCount7);
            }
        });
        ss.set("random", new SFunction() {
            @Override
            public Value run(Starscript ss7, int argCount6) {
                return random(ss7, argCount6);
            }
        });

        // Strings
        ss.set("string", new SFunction() {
            @Override
            public Value run(Starscript ss6, int argCount5) {
                return string(ss6, argCount5);
            }
        });
        ss.set("toUpper", new SFunction() {
            @Override
            public Value run(Starscript ss5, int argCount4) {
                return toUpper(ss5, argCount4);
            }
        });
        ss.set("toLower", new SFunction() {
            @Override
            public Value run(Starscript ss4, int argCount3) {
                return toLower(ss4, argCount3);
            }
        });
        ss.set("contains", new SFunction() {
            @Override
            public Value run(Starscript ss3, int argCount2) {
                return contains(ss3, argCount2);
            }
        });
        ss.set("replace", new SFunction() {
            @Override
            public Value run(Starscript ss2, int argCount1) {
                return replace(ss2, argCount1);
            }
        });
        ss.set("pad", new SFunction() {
            @Override
            public Value run(Starscript ss1, int argCount) {
                return pad(ss1, argCount);
            }
        });
    }

    // Numbers

    public static Value round(Starscript ss, int argCount) {
        if (argCount == 1) {
            double a = ss.popNumber("Argument to round() needs to be a number.");
            return Value.number(Math.round(a));
        }
        else if (argCount == 2) {
            double b = ss.popNumber("Second argument to round() needs to be a number.");
            double a = ss.popNumber("First argument to round() needs to be a number.");

            double x = Math.pow(10, (int) b);
            return Value.number(Math.round(a * x) / x);
        }
        else {
            ss.error("round() requires 1 or 2 arguments, got %d.", argCount);
            return null;
        }
    }

    public static Value roundToString(Starscript ss, int argCount) {
        if (argCount == 1) {
            double a = ss.popNumber("Argument to round() needs to be a number.");
            return Value.string(Double.toString(Math.round(a)));
        }
        else if (argCount == 2) {
            double b = ss.popNumber("Second argument to round() needs to be a number.");
            double a = ss.popNumber("First argument to round() needs to be a number.");

            double x = Math.pow(10, (int) b);
            return Value.string(Double.toString(Math.round(a * x) / x));
        }
        else {
            ss.error("round() requires 1 or 2 arguments, got %d.", argCount);
            return null;
        }
    }

    public static Value floor(Starscript ss, int argCount) {
        if (argCount != 1) ss.error("floor() requires 1 argument, got %d.", argCount);
        double a = ss.popNumber("Argument to floor() needs to be a number.");
        return Value.number(Math.floor(a));
    }

    public static Value ceil(Starscript ss, int argCount) {
        if (argCount != 1) ss.error("ceil() requires 1 argument, got %d.", argCount);
        double a = ss.popNumber("Argument to ceil() needs to be a number.");
        return Value.number(Math.ceil(a));
    }

    public static Value abs(Starscript ss, int argCount) {
        if (argCount != 1) ss.error("abs() requires 1 argument, got %d.", argCount);
        double a = ss.popNumber("Argument to abs() needs to be a number.");
        return Value.number(Math.abs(a));
    }

    public static Value random(Starscript ss, int argCount) {
        if (argCount == 0) return Value.number(rand.nextDouble());
        else if (argCount == 2) {
            double max = ss.popNumber("Second argument to random() needs to be a number.");
            double min = ss.popNumber("First argument to random() needs to be a number.");

            return Value.number(min + (max - min) * rand.nextDouble());
        }

        ss.error("random() requires 0 or 2 arguments, got %d.", argCount);
        return Value.null_();
    }

    // Strings

    private static Value string(Starscript ss, int argCount) {
        if (argCount != 1) ss.error("string() requires 1 argument, got %d.", argCount);
        return Value.string(ss.pop().toString());
    }

    public static Value toUpper(Starscript ss, int argCount) {
        if (argCount != 1) ss.error("toUpper() requires 1 argument, got %d.", argCount);
        String a = ss.popString("Argument to toUpper() needs to be a string.");
        return Value.string(a.toUpperCase());
    }

    public static Value toLower(Starscript ss, int argCount) {
        if (argCount != 1) ss.error("toLower() requires 1 argument, got %d.", argCount);
        String a = ss.popString("Argument to toLower() needs to be a string.");
        return Value.string(a.toLowerCase());
    }

    public static Value contains(Starscript ss, int argCount) {
        if (argCount != 2) ss.error("replace() requires 2 arguments, got %d.", argCount);

        String search = ss.popString("Second argument to contains() needs to be a string.");
        String string = ss.popString("First argument to contains() needs to be a string.");

        return Value.bool(string.contains(search));
    }

    public static Value replace(Starscript ss, int argCount) {
        if (argCount != 3) ss.error("replace() requires 3 arguments, got %d.", argCount);

        String to = ss.popString("Third argument to replace() needs to be a string.");
        String from = ss.popString("Second argument to replace() needs to be a string.");
        String string = ss.popString("First argument to replace() needs to be a string.");

        return Value.string(string.replace(from, to));
    }

    public static Value pad(Starscript ss, int argCount) {
        if (argCount != 2) ss.error("pad() requires 2 arguments, got %d.", argCount);

        int width = (int) ss.popNumber("Second argument to pad() needs to be a number.");
        String text = ss.pop().toString();

        if (text.length() >= Math.abs(width)) return Value.string(text);

        char[] padded = new char[Math.max(text.length(), Math.abs(width))];

        if (width >= 0) {
            int padLength = width - text.length();
            for (int i = 0; i < padLength; i++) padded[i] = ' ';
            for (int i = 0; i < text.length(); i++) padded[padLength + i] = text.charAt(i);
        }
        else {
            for (int i = 0; i < text.length(); i++) padded[i] = text.charAt(i);
            for (int i = 0; i < Math.abs(width) - text.length(); i++) padded[text.length() + i] = ' ';
        }

        return Value.string(new String(padded));
    }
}
