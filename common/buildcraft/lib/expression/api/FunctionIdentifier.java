package buildcraft.lib.expression.api;

import java.util.Objects;

public final class FunctionIdentifier {
    public final String lowerCaseName;
    public final ArgumentCounts args;
    private final int hash;

    public FunctionIdentifier(String lowerCaseName, ArgumentCounts args) {
        this.lowerCaseName = lowerCaseName;
        this.args = args;
        this.hash = Objects.hash(lowerCaseName, args);
    }

    @Override
    public String toString() {
        return lowerCaseName + "(" + args + ")";
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        FunctionIdentifier other = (FunctionIdentifier) obj;
        return lowerCaseName.equalsIgnoreCase(other.lowerCaseName)//
            && args.equals(other.args);
    }
}
