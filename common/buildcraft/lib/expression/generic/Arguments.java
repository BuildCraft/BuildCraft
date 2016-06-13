package buildcraft.lib.expression.generic;

import com.google.common.collect.ImmutableList;

public class Arguments {
    public enum ArgType {
        LONG,
        DOUBLE,
        BOOLEAN,
        STRING
    }

    public static final ArgumentCounts NO_COUNT = new ArgumentCounts();
    public static final Arguments NO_ARGS = NO_COUNT.createArgs();

    public final long[] longs;
    public final double[] doubles;
    public final boolean[] booleans;
    public final String[] strings;

    public Arguments(ArgumentCounts counts) {
        this.longs = new long[counts.longs];
        this.doubles = new double[counts.doubles];
        this.booleans = new boolean[counts.booleans];
        this.strings = new String[counts.strings];
    }

    public static class ArgumentCounts {
        public final int longs, doubles, booleans, strings;
        public final ImmutableList<ArgType> order;

        public ArgumentCounts(ArgType... types) {
            order = ImmutableList.copyOf(types);
            int longs = 0;
            int doubles = 0;
            int booleans = 0;
            int strings = 0;
            for (ArgType type : types) {
                if (type == ArgType.LONG) longs++;
                if (type == ArgType.DOUBLE) doubles++;
                if (type == ArgType.BOOLEAN) booleans++;
                if (type == ArgType.STRING) strings++;
            }
            this.longs = longs;
            this.doubles = doubles;
            this.booleans = booleans;
            this.strings = strings;
        }

        public Arguments createArgs() {
            return new Arguments(this);
        }

        public boolean matches(Arguments args) {
            if (this == NO_COUNT && args == NO_ARGS) {
                return true;
            }
            return longs == args.longs.length//
                && doubles == args.doubles.length//
                && booleans == args.booleans.length//
                && strings == args.strings.length;
        }
    }
}
