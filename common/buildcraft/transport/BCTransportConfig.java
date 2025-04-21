/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.api.BCModules;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.EnumPipeColourType;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.lib.config.BCConfig;
import buildcraft.lib.config.ConfigCategory;
import buildcraft.lib.config.Configuration;
import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.misc.MathUtil;

public class BCTransportConfig {
    public enum PowerLossMode {
        LOSSLESS,
        PERCENTAGE,
        ABSOLUTE;

        public static final PowerLossMode DEFAULT = LOSSLESS;
        public static final PowerLossMode[] VALUES = values();
    }

    private static Configuration config;

    private static final long MJ_REQ_MILLIBUCKET_MIN = 100;
    private static final long MJ_REQ_ITEM_MIN = 50_000;

    public static long mjPerMillibucket = 1_000;
    public static long mjPerItem = MjAPI.MJ;
    public static int baseFlowRate = 10;
    public static boolean fluidPipeColourBorder;
    public static PowerLossMode lossMode = PowerLossMode.DEFAULT;

    private static ConfigCategory<Long> propMjPerMillibucket;
    private static ConfigCategory<Long> propMjPerItem;
    private static ConfigCategory<Integer> propBaseFlowRate;
    private static ConfigCategory<Boolean> propFluidPipeColourBorder;
    private static ConfigCategory<PowerLossMode> propLossMode;

    public static void preInit() {
//        Configuration config = BCCoreConfig.config;
        BCModules module = BCModules.TRANSPORT;
        config = new Configuration(module);
        createProps();

        reloadConfig();
//        MinecraftForge.EVENT_BUS.register(BCTransportConfig.class);
        BCConfig.registerReloadListener(module, BCTransportConfig::reloadConfig);
    }

    public static void createProps() {
        String general = "general";
        String display = "display";
        String experimental = "experimental";

        EnumRestartRequirement world = EnumRestartRequirement.WORLD;

        propMjPerMillibucket = config
                .defineInRange(general,
                        "",
                        world,
                        "pipes.mjPerMillibucket", mjPerMillibucket, MJ_REQ_MILLIBUCKET_MIN);
        propMjPerItem = config
                .defineInRange(general,
                        "",
                        world,
                        "pipes.mjPerItem", mjPerItem, MJ_REQ_ITEM_MIN);
        propBaseFlowRate = config
                .defineInRange(general,
                        "",
                        world,
                        "pipes.baseFluidRate", baseFlowRate, 1, 40);

        propFluidPipeColourBorder = config
                .define(display,
                        "",
                        world,
                        "pipes.fluidColourIsBorder", true);

        propLossMode = config
                .defineEnum(experimental,
                        "",
                        world,
                        "kinesisLossMode", PowerLossMode.LOSSLESS);
    }

    // public static void reloadConfig(EnumRestartRequirement restarted)
    public static void reloadConfig() {
//        if (EnumRestartRequirement.WORLD.hasBeenRestarted(restarted)) {
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
//        }

        saveConfigs();
    }

    private static void fluidTransfer(PipeDefinition def, int rate, int delay) {
        PipeApi.fluidTransferData.put(def, new PipeApi.FluidTransferInfo(rate, delay));
    }

    private static void powerTransfer(PipeDefinition def, int transferMultiplier, int resistanceDivisor, boolean recv) {
        long transfer = MjAPI.MJ * transferMultiplier;
        long resistance = MjAPI.MJ / resistanceDivisor;
        PipeApi.powerTransferData.put(def, PowerTransferInfo.createFromResistance(transfer, resistance, recv));
    }

    public static void saveConfigs() {
        if (config.hasChanged()) {
            config.save();
        }
    }

//    @SubscribeEvent
//    public static void onConfigChange(OnConfigChangedEvent cce) {
//        if (BCModules.isBcMod(cce.getModID())) {
//            EnumRestartRequirement req = EnumRestartRequirement.NONE;
//            if (Loader.instance().isInState(LoaderState.AVAILABLE)) {
//                // The loaders state will be LoaderState.SERVER_STARTED when we are in a world
//                req = EnumRestartRequirement.WORLD;
//            }
//            reloadConfig(req);
//        }
//    }
}
