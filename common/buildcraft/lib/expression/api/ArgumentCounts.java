package buildcraft.lib.expression.api;

import java.util.Locale;

import com.google.common.collect.ImmutableList;

public final class ArgumentCounts {
    public static final ArgumentCounts NO_ARGS = new ArgumentCounts();

    public final int longs, doubles, booleans, strings;
    public final ImmutableList<ArgType> order;
    private final int hash;

    public ArgumentCounts(ArgType... types) {
        order = ImmutableList.copyOf(types);
        int longs = 0;
        int doubles = 0;
        int booleans = 0;
        int strings = 0;
        for (ArgType type : types) {
            if (type == ArgType.LONG) longs++;
            if (type == ArgType.DOUBLE) doubles++;
            if (type == ArgType.BOOL) booleans++;
            if (type == ArgType.STRING) strings++;
        }
        this.longs = longs;
        this.doubles = doubles;
        this.booleans = booleans;
        this.strings = strings;
        this.hash = order.hashCode();
    }

    public Arguments createArgs() {
        return new Arguments(this);
    }

    public boolean canCastTo(ArgumentCounts counts) {
        if (order.size() != counts.order.size()) {
            System.out.println("Cannot cast from " + counts.order + " to " + order);
            return false;
        }
        for (int i = 0; i < order.size(); i++) {
            ArgType from = order.get(i);
            ArgType to = counts.order.get(i);
            if (!from.canCastTo(to)) {
                return false;
            }
        }
        return true;
    }

    /** Checks to see if the given arguments can be used by these counts. Note that this does *not* check the order. */
    // TODO: Check the order!
    public boolean matches(Arguments args) {
        if (this == NO_ARGS && args == Arguments.NO_ARGS) {
            return true;
        }
        return longs == args.longs.length//
            && doubles == args.doubles.length//
            && booleans == args.bools.length//
            && strings == args.strings.length;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj instanceof ArgumentCounts) {
            ArgumentCounts other = (ArgumentCounts) obj;
            // longs, doubles, strings, booleans are implied by the order
            // So we don't need to compare them
            return order.equals(other.order);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return order.toString().toLowerCase(Locale.ROOT);
    }
}
