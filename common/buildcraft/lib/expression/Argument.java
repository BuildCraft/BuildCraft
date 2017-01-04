package buildcraft.lib.expression;

import java.util.Locale;

import buildcraft.lib.expression.api.NodeType;

public class Argument {
    public final String name;
    public final NodeType type;

    public Argument(String name, NodeType type) {
        this.name = name;
        this.type = type;
    }

    public static Argument argLong(String name) {
        return new Argument(name, NodeType.LONG);
    }

    public static Argument argDouble(String name) {
        return new Argument(name, NodeType.DOUBLE);
    }

    public static Argument argBoolean(String name) {
        return new Argument(name, NodeType.BOOLEAN);
    }

    public static Argument argString(String name) {
        return new Argument(name, NodeType.STRING);
    }

    @Override
    public String toString() {
        return type.name().toLowerCase(Locale.ROOT) + " '" + name + "'";
    }
}
