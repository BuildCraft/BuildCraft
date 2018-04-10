/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gate;

import java.util.Locale;

public enum EnumGateModifier {
    NO_MODIFIER(0, 0, 1),
    LAPIS(1, 0, 1),
    QUARTZ(1, 1, 2),
    DIAMOND(3, 3, 2);

    public static final EnumGateModifier[] VALUES = values();

    public final int triggerParams, actionParams;
    public final int slotDivisor;
    public final String tag = name().toLowerCase(Locale.ROOT);

    EnumGateModifier(int triggerParams, int actionParams, int slotDivisor) {
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
