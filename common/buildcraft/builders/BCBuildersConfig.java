/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import net.minecraftforge.common.config.Property;

import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.misc.MathUtil;

import buildcraft.core.BCCoreConfig;

public class BCBuildersConfig {
    /** Blueprints that save larger than this are stored externally, smaller ones are stored directly in the item. */
    public static int bptStoreExternalThreshold = 20_000;

    /** The minimum height that all quarry frames must be. */
    public static int quarryFrameMinHeight = 4;

    /** If true then the frame will move with the drill in both axis, if false then only 1 axis will follow the
     * drill. */
    public static boolean quarryFrameMoveBoth;

    public static int quarryMaxTasksPerTick = 4;
    public static int quarryTaskPowerDivisor = 2;
    public static double quarryMaxFrameMoveSpeed = 0;
    public static double quarryMaxBlockMineRate = 0;

    /** Client-side config to enable stencils-based drawing for the architect table. */
    public static boolean enableStencil = true;

    private static Property propBptStoreExternalThreshold;
    private static Property propQuarryFrameMinHeight;
    private static Property propQuarryFrameMoveBoth;
    private static Property propQuarryMaxTasksPerTick;
    private static Property propQuarryPowerDivisor;
    private static Property propQuarryMaxFrameSpeed;
    private static Property propQuarryMaxBlockMineRate;
    private static Property propEnableStencil;

    static Property internalStencilCrashTest;

    public static void preInit() {
        EnumRestartRequirement none = EnumRestartRequirement.NONE;
        EnumRestartRequirement game = EnumRestartRequirement.GAME;

        propBptStoreExternalThreshold = BCCoreConfig.config.get("general", "bptStoreExternalThreshold", 20_000);
        none.setTo(propBptStoreExternalThreshold);

        propQuarryFrameMinHeight = BCCoreConfig.config.get("general", "quarryFrameMinHeight", 4);
        propQuarryFrameMinHeight.setComment("The minimum height that all quarry frames must be. A value of 1 will look strange when it drills the uppermost layer.");
        propQuarryFrameMinHeight.setMinValue(1);
        none.setTo(propQuarryFrameMinHeight);

        propQuarryFrameMoveBoth = BCCoreConfig.config.get("display", "quarryFrameMoveBoth", false);
        propQuarryFrameMoveBoth.setComment("If true then the quarry frame will move with both of its axis rather than just one.");
        none.setTo(propQuarryFrameMoveBoth);

        propQuarryMaxTasksPerTick = BCCoreConfig.config.get("general", "quarryMaxTasksPerTick", 4);
        propQuarryMaxTasksPerTick.setComment("The maximum number of tasks that the quarry will do per tick."
            + "\n(Where a task is either breaking a block, or moving the frame)");
        propQuarryMaxTasksPerTick.setMinValue(1).setMaxValue(20);
        none.setTo(propQuarryMaxTasksPerTick);

        propQuarryPowerDivisor = BCCoreConfig.config.get("general", "quarryPowerDivisor", 2);
        propQuarryPowerDivisor.setComment("1 divided by this value is added to the power cost for each additional task done per tick."
            + "\nA value of 0 disables this behaviour.");
        propQuarryPowerDivisor.setMinValue(0).setMaxValue(100);
        none.setTo(propQuarryPowerDivisor);

        propQuarryMaxFrameSpeed = BCCoreConfig.config.get("general", "quarryMaxFrameSpeed", 0.0);
        propQuarryMaxFrameSpeed.setComment("The maximum number of blocks that a quarry is allowed to move, per second."
            + "\nA value of 0 means no limit.");
        propQuarryMaxFrameSpeed.setMinValue(0.0).setMaxValue(5120.0);
        none.setTo(propQuarryMaxFrameSpeed);

        propQuarryMaxBlockMineRate = BCCoreConfig.config.get("general", "quarryMaxBlockMineRate", 0.0);
        propQuarryMaxBlockMineRate.setComment("The maximum number of blocks that the quarry is allowed to mine each second."
            + "\nA value of 0 means no limit, and a value of 0.5 will mine up to half a block per second.");
        propQuarryMaxBlockMineRate.setMinValue(0.0).setMaxValue(1000.0);
        none.setTo(propQuarryMaxBlockMineRate);

        propEnableStencil = BCCoreConfig.config.get("display", "enableStencil", true);
        propEnableStencil.setComment("If true then the architect table will correctly hide it's translucent parts behind surrounding terrain. (This looks better)");
        none.setTo(propEnableStencil);

        internalStencilCrashTest = BCCoreConfig.config.get("internal", "force_disable_stencil", false);
        internalStencilCrashTest.setComment("Use display.enableStencil instead of this!");
        none.setTo(internalStencilCrashTest);
        BCCoreConfig.config.getCategory("internal").setShowInGui(false);
        BCCoreConfig.saveConfigs();

        reloadConfig(EnumRestartRequirement.GAME);
        BCCoreConfig.addReloadListener(BCBuildersConfig::reloadConfig);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        bptStoreExternalThreshold = propBptStoreExternalThreshold.getInt();
        quarryFrameMinHeight = propQuarryFrameMinHeight.getInt();
        quarryFrameMoveBoth = propQuarryFrameMoveBoth.getBoolean();
        enableStencil = propEnableStencil.getBoolean();
        quarryMaxTasksPerTick = MathUtil.clamp(propQuarryMaxTasksPerTick.getInt(), 0, 20);
        quarryTaskPowerDivisor = MathUtil.clamp(propQuarryPowerDivisor.getDouble(), 0, 100);
        quarryMaxFrameMoveSpeed = MathUtil.clamp(propQuarryMaxFrameSpeed.getDouble(), 0, 5120.0);
        quarryMaxBlockMineRate = MathUtil.clamp(propQuarryMaxBlockMineRate.getDouble(), 0, 1000.0);
    }
}
