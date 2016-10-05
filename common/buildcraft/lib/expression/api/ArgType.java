package buildcraft.lib.expression.api;

import buildcraft.lib.expression.GenericExpressionCompiler;

public enum ArgType {
    LONG,
    DOUBLE,
    BOOL,
    STRING;

    public boolean canCastTo(ArgType other) {
        boolean ans = canCastTo0(other);
        if (ans) {
            GenericExpressionCompiler.debugPrintln("Can cast from " + this + " to " + other);
        } else {
            GenericExpressionCompiler.debugPrintln("NO CASTING FROM " + this + " TO " + other + " EVER!");
        }
        return ans;
    }

    private boolean canCastTo0(ArgType other) {
        if (this == other) {
            return true;
        }
        switch (this) {
            case BOOL:
                return other == ArgType.STRING;
            case DOUBLE:
                return other == ArgType.STRING;
            case LONG:
                return other == ArgType.DOUBLE || other == ArgType.STRING;
            case STRING:
            default:
                return false;
        }
    }
}