/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders;

import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class BCBuildersConfig {
	
	public static ForgeConfigSpec config;
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

    private static IntValue propBptStoreExternalThreshold;
    private static IntValue propQuarryFrameMinHeight;
    private static BooleanValue propQuarryFrameMoveBoth;
    private static IntValue propQuarryMaxTasksPerTick;
    private static IntValue propQuarryPowerDivisor;
    private static DoubleValue propQuarryMaxFrameSpeed;
    private static DoubleValue propQuarryMaxBlockMineRate;
    private static BooleanValue propEnableStencil;

    static BooleanValue internalStencilCrashTest;

    public static void preInit() {
    	ForgeConfigSpec.Builder con_builder = new ForgeConfigSpec.Builder();
    	
    	con_builder.push("general");
    	
        propBptStoreExternalThreshold = con_builder.defineInRange("bptStoreExternalThreshold", 20_000, 0, 100_000);

        propQuarryFrameMinHeight = con_builder.comment("The minimum height that all quarry frames must be. A value of -63 will look strange when it drills the uppermost layer.")
        		.defineInRange("quarryFrameMinHeight", 4, 1, 512);


        propQuarryMaxTasksPerTick = con_builder.comment("The maximum number of tasks that the quarry will do per tick."
                + "\n(Where a task is either breaking a block, or moving the frame)")
        		.defineInRange("quarryMaxTasksPerTick", 4, 1, 20);

        propQuarryPowerDivisor = con_builder.comment("1 divided by this value is added to the power cost for each additional task done per tick."
                + "\nA value of 0 disables this behaviour.")
        		.defineInRange("quarryPowerDivisor", 2, 0, 100);

        propQuarryMaxFrameSpeed = con_builder.comment("The maximum number of blocks that a quarry is allowed to move, per second."
                + "\nA value of 0 means no limit.").defineInRange("quarryMaxFrameSpeed", 0.0, 0.0, 5120.0);

        propQuarryMaxBlockMineRate = con_builder.comment("The maximum number of blocks that the quarry is allowed to mine each second."
                + "\nA value of 0 means no limit, and a value of 0.5 will mine up to half a block per second.")
        		.defineInRange("quarryMaxBlockMineRate", 0.0, 0.0, 1000.0);
        
        con_builder.pop();
        con_builder.push("display");
        
        
        propQuarryFrameMoveBoth = con_builder.comment("If true then the quarry frame will move with both of its axis rather than just one.")
        		.define("quarryFrameMoveBoth", false);
        
        propEnableStencil = con_builder.comment("If true then the architect table will correctly hide it's translucent parts behind surrounding terrain. (This looks better)")
        		.define("enableStencil", true);

        con_builder.pop();
        con_builder.push("internal");
        
        internalStencilCrashTest = con_builder.comment("Use display.enableStencil instead of this!")
        		.define("force_disable_stencil", false);
        
        //con_builder.getCategory("internal").setShowInGui(false);
        con_builder.pop();
        config = con_builder.build();
//        reloadConfig(EnumRestartRequirement.GAME);
//        BCCoreConfig.addReloadListener(BCBuildersConfig::reloadConfig);
    }
    
    @SubscribeEvent
    public static void onReloadConfig(final ModConfigEvent.Reloading restarted) {
    	reloadConfig(restarted.getConfig().getModId());
    }
    
    @SubscribeEvent
    public static void onLoadConfig(final ModConfigEvent.Loading load) {
    	reloadConfig(load.getConfig().getModId());
    }

    public static void reloadConfig(String modid) {
    	if(!modid.equals(BCBuilders.MODID))return ;
        bptStoreExternalThreshold = propBptStoreExternalThreshold.get();
        quarryFrameMinHeight = propQuarryFrameMinHeight.get();
        quarryFrameMoveBoth = propQuarryFrameMoveBoth.get();
        enableStencil = propEnableStencil.get();
        quarryMaxTasksPerTick = Mth.clamp(propQuarryMaxTasksPerTick.get(), 0, 20);
        quarryTaskPowerDivisor = Mth.clamp(propQuarryPowerDivisor.get(), 0, 100);
        quarryMaxFrameMoveSpeed = Mth.clamp(propQuarryMaxFrameSpeed.get(), 0, 5120.0);
        quarryMaxBlockMineRate = Mth.clamp(propQuarryMaxBlockMineRate.get(), 0, 1000.0);
    }
}
