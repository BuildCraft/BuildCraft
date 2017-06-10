/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import net.minecraftforge.common.config.Property;

import buildcraft.lib.config.EnumRestartRequirement;

import buildcraft.core.BCCoreConfig;

public class BCBuildersConfig {
    /** Blueprints that save larger than this are stored externally, smaller ones are stored directly in the item. */
    public static int bptStoreExternalThreshold = 20_000;

    /** The minimum height that all quarry frames must be. */
    public static int quarryFrameMinHeight = 4;

    /** If true then the frame will move with the drill in both axis, if false then only 1 axis will follow the
     * drill. */
    public static boolean quarryFrameMoveBoth;

    private static Property propBptStoreExternalThreshold;
    private static Property propQuarryFrameMinHeight;
    private static Property propQuarryFrameMoveBoth;

    public static void preInit() {
        EnumRestartRequirement none = EnumRestartRequirement.NONE;
        EnumRestartRequirement game = EnumRestartRequirement.GAME;

        propBptStoreExternalThreshold = BCCoreConfig.config.get("general", "bptStoreExternalThreshold", 20_000);
        none.setTo(propBptStoreExternalThreshold);

        propQuarryFrameMinHeight = BCCoreConfig.config.get("general", "quarryFrameMinHeight", 4);
        propQuarryFrameMinHeight.setComment("The minimum height that all quarry frames must be. A value of 1 will look strange when it drills the toppermost layer.");
        propQuarryFrameMinHeight.setMinValue(1);
        none.setTo(propQuarryFrameMinHeight);

        propQuarryFrameMoveBoth = BCCoreConfig.config.get("display", "quarryFrameMoveBoth", false);
        propQuarryFrameMoveBoth.setComment("If true then the quarry frame will move with both of its axis rather than just one.");
        none.setTo(propQuarryFrameMoveBoth);

        reloadConfig(EnumRestartRequirement.GAME);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        bptStoreExternalThreshold = propBptStoreExternalThreshold.getInt();
        quarryFrameMinHeight = propQuarryFrameMinHeight.getInt();
        quarryFrameMoveBoth = propQuarryFrameMoveBoth.getBoolean();
    }
}
