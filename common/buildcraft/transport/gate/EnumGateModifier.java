package buildcraft.transport.gate;

import java.util.Locale;

public enum EnumGateModifier {
    NO_MODIFIER(0, 0, 1),
    LAPIS(1, 0, 1),
    QUARTZ(1, 1, 2),
    DIAMOND(2, 2, 2),
    PRISMARINE(3, 3, 2);

    public static final EnumGateModifier[] VALUES = values();

    public final int triggerParams, actionParams;
    public final int slotDivisor;
    public final String tag = name().toLowerCase(Locale.ROOT);

    private EnumGateModifier(int triggerParams, int actionParams, int slotDivisor) {
        this.triggerParams = triggerParams;
        this.actionParams = actionParams;
        this.slotDivisor = slotDivisor;
    }

    public static EnumGateModifier getByOrdinal(int ord) {
        if (ord < 0 || ord >= VALUES.length) {
            return EnumGateModifier.NO_MODIFIER;
        }
        return VALUES[ord];
    }
}
