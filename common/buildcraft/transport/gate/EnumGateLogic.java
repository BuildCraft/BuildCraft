package buildcraft.transport.gate;

import java.util.Locale;

public enum EnumGateLogic {
    AND,
    OR;

    public static final EnumGateLogic[] VALUES = values();

    public final String tag = name().toLowerCase(Locale.ROOT);

    public static EnumGateLogic getByOrdinal(int ord) {
        if (ord < 0 || ord >= VALUES.length) {
            return EnumGateLogic.AND;
        }
        return VALUES[ord];
    }
}
