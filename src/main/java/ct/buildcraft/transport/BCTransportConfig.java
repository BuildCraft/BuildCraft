/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport;

import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.transport.pipe.EnumPipeColourType;
import ct.buildcraft.api.transport.pipe.PipeApi;
import ct.buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import ct.buildcraft.api.transport.pipe.PipeDefinition;
import ct.buildcraft.lib.misc.MathUtil;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class BCTransportConfig {
    public enum PowerLossMode {
        LOSSLESS,
        PERCENTAGE,
        ABSOLUTE;

        public static final PowerLossMode DEFAULT = LOSSLESS;
        public static final PowerLossMode[] VALUES = values();
    }
    
    public static ForgeConfigSpec config;

    private static final long MJ_REQ_MILLIBUCKET_MIN = 100;
    private static final long MJ_REQ_ITEM_MIN = 50_000;

    public static long mjPerMillibucket = 1_000;
    public static long mjPerItem = MjAPI.MJ;
    public static int baseFlowRate = 10;
    public static boolean fluidPipeColourBorder;
    public static PowerLossMode lossMode = PowerLossMode.DEFAULT;

    private static IntValue propMjPerMillibucket;
    private static IntValue propMjPerItem;
    private static IntValue propBaseFlowRate;
    private static BooleanValue propFluidPipeColourBorder;
    private static EnumValue<PowerLossMode> propLossMode;

    public static void preInit() {
    	ForgeConfigSpec.Builder con_config = new ForgeConfigSpec.Builder();
    	con_config.push("general");
        propMjPerMillibucket = con_config.worldRestart()
        		.defineInRange("pipes.mjPerMillibucket", (int) mjPerMillibucket,(int) MJ_REQ_MILLIBUCKET_MIN,Integer.MAX_VALUE);

        propMjPerItem = con_config.worldRestart()
        		.defineInRange("pipes.mjPerItem", (int) mjPerItem, (int) MJ_REQ_ITEM_MIN, Integer.MAX_VALUE);

        propBaseFlowRate = con_config.worldRestart()
        		.defineInRange("pipes.baseFluidRate",baseFlowRate, 1, 40);
        con_config.pop();
        con_config.push("display");
        propFluidPipeColourBorder = con_config.worldRestart()
        		.define("pipes.fluidColourIsBorder",true);
        con_config.pop();
        con_config.push("experimental");
        propLossMode = con_config.worldRestart()
        		.defineEnum("kinesisLossMode", PowerLossMode.LOSSLESS, PowerLossMode.values());

        config = con_config.build();
        MinecraftForge.EVENT_BUS.register(BCTransportConfig.class);
    }

    public static void reloadConfig() {

    	mjPerMillibucket = propMjPerMillibucket.get();
    	if (mjPerMillibucket < MJ_REQ_MILLIBUCKET_MIN) {
    		mjPerMillibucket = MJ_REQ_MILLIBUCKET_MIN;
    	}
    		
    	mjPerItem = propMjPerItem.get();
    	if (mjPerItem < MJ_REQ_ITEM_MIN) {
    		mjPerItem = MJ_REQ_ITEM_MIN;
    	}
    	
    	baseFlowRate = MathUtil.clamp(propBaseFlowRate.get(), 1, 40);
    	int basePowerRate = 4;

    	fluidPipeColourBorder = propFluidPipeColourBorder.get();
    	PipeApi.flowFluids.fallbackColourType =
    		fluidPipeColourBorder ? EnumPipeColourType.BORDER_INNER : EnumPipeColourType.TRANSLUCENT;

    	lossMode = propLossMode.get();

    	fluidTransfer(BCTransportPipes.cobbleFluid, baseFlowRate, 10);
    	fluidTransfer(BCTransportPipes.woodFluid, baseFlowRate, 10);

    	fluidTransfer(BCTransportPipes.stoneFluid, baseFlowRate * 2, 10);
    	fluidTransfer(BCTransportPipes.sandstoneFluid, baseFlowRate * 2, 10);

    	fluidTransfer(BCTransportPipes.clayFluid, baseFlowRate * 4, 10);
    	fluidTransfer(BCTransportPipes.ironFluid, baseFlowRate * 4, 10);
    	fluidTransfer(BCTransportPipes.quartzFluid, baseFlowRate * 4, 10);

    	fluidTransfer(BCTransportPipes.diamondFluid, baseFlowRate * 8, 10);
    	fluidTransfer(BCTransportPipes.diaWoodFluid, baseFlowRate * 8, 10);
    	fluidTransfer(BCTransportPipes.goldFluid, baseFlowRate * 8, 2);
    	fluidTransfer(BCTransportPipes.voidFluid, baseFlowRate * 8, 10);

    	powerTransfer(BCTransportPipes.cobblePower, basePowerRate, 16, false);
    	powerTransfer(BCTransportPipes.stonePower, basePowerRate * 2, 32, false);
    	powerTransfer(BCTransportPipes.woodPower, basePowerRate * 4, 128, true);
    	powerTransfer(BCTransportPipes.sandstonePower, basePowerRate * 4, 32, false);
    	powerTransfer(BCTransportPipes.quartzPower, basePowerRate * 8, 32, false);
    	// powerTransfer(BCTransportPipes.ironPower, basePowerRate * 8, false);
    	powerTransfer(BCTransportPipes.goldPower, basePowerRate * 16, 32, false);
    	// powerTransfer(BCTransportPipes.diamondPower, basePowerRate * 32, false);
    }	

    private static void fluidTransfer(PipeDefinition def, int rate, int delay) {
        PipeApi.fluidTransferData.put(def, new PipeApi.FluidTransferInfo(rate, delay));
    }

    private static void powerTransfer(PipeDefinition def, int transferMultiplier, int resistanceDivisor, boolean recv) {
        long transfer = MjAPI.MJ * transferMultiplier;
        long resistance = MjAPI.MJ / resistanceDivisor;
        PipeApi.powerTransferData.put(def, PowerTransferInfo.createFromResistance(transfer, resistance, recv));
    }

    @SubscribeEvent
    public static void onConfigChange(ModConfigEvent.Reloading cce) {
        if (cce.getConfig().getModId() == BCTransport.MODID) {
            reloadConfig();
        }
    }
}
