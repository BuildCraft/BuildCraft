/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport;

import buildcraft.api.mj.MjAPI;

// STUB(R.Chen): Forge config (Configuration/Property) removed. Fabric Config API migration in Phase 4E.
// All static fields retain their original defaults.
public class BCTransportConfig {

    public enum PowerLossMode {
        LOSSLESS, PERCENTAGE, ABSOLUTE;

        public static final PowerLossMode DEFAULT = LOSSLESS;
        public static final PowerLossMode[] VALUES = values();
    }

    private static final long MJ_REQ_MILLIBUCKET_MIN = 100;
    private static final long MJ_REQ_ITEM_MIN = 50_000;

    public static long mjPerMillibucket = 1_000;
    public static long mjPerItem = MjAPI.MJ;
    public static int baseFlowRate = 10;
    public static int basePowerRate = 4;
    public static int baseRfRate = 40;
    public static boolean fluidPipeColourBorder = false;
    public static boolean disableRfPipe = false;
    public static boolean powerPipeUseOldMjTexture = false;
    public static PowerLossMode lossMode = PowerLossMode.DEFAULT;
    public static int networkUpdateRate = 4;

    public static void preInit() {
        // STUB(R.Chen): Forge config removed — Phase 4E Fabric Config API.
    }

    public static void reloadConfig() {
        // STUB(R.Chen): Forge config reload removed — Phase 4E Fabric Config API.
    }
}
