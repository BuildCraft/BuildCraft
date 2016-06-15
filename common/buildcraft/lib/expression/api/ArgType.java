package buildcraft.lib.expression.api;

public enum ArgType {
    LONG,
    DOUBLE,
    BOOL,
    STRING;

    public boolean canCastTo(ArgType other) {
        boolean ans = canCastTo0(other);
        if (ans) {
            System.out.println("Can cast from " + this + " to " + other);
        } else {
            System.out.println("NO CASTING FROM " + this + " TO " + other + " EVER!");
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