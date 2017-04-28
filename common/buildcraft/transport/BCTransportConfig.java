package buildcraft.transport;

import java.util.Locale;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCLog;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.core.BCCoreConfig;
import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.misc.MathUtil;

public class BCTransportConfig {
    public enum PowerLossMode {
        LOSSLESS,
        PERCENTAGE,
        ABSOLUTE;

        public static final PowerLossMode DEFAULT = LOSSLESS;
        public static final PowerLossMode[] VALUES = values();

        public final String configName = name().toLowerCase(Locale.ROOT);

        public static PowerLossMode get(String value, boolean warn) {
            char c = value.length() == 0 ? ' ' : value.charAt(0);
            switch (c) {
                case 'l':
                case 'L':
                    return getAssumingEqual(value, LOSSLESS, warn);
                case 'p':
                case 'P':
                    return getAssumingEqual(value, PERCENTAGE, warn);
                case 'a':
                case 'A':
                    return getAssumingEqual(value, ABSOLUTE, warn);
            }
            return getAssumingEqual(value, DEFAULT, warn);
        }

        private static PowerLossMode getAssumingEqual(String value, PowerLossMode mode, boolean warn) {
            if (warn && !mode.configName.equalsIgnoreCase(value)) {
                BCLog.logger.warn("[transport.config] Unknown PowerLossMode '" + value + "', assuming " + mode.configName);
            }
            return mode;
        }
    }

    private static final long MJ_REQ_MILLIBUCKET_MIN = 100;
    private static final long MJ_REQ_ITEM_MIN = 50_000;

    public static long mjPerMillibucket = 1_000;
    public static long mjPerItem = MjAPI.MJ;
    public static int baseFlowRate = 10;
    public static PowerLossMode lossMode = PowerLossMode.DEFAULT;

    private static Property propMjPerMillibucket;
    private static Property propMjPerItem;
    private static Property propBaseFlowRate;
    private static Property propLossMode;

    public static void preInit() {
        Configuration config = BCCoreConfig.config;
        propMjPerMillibucket = config.get("general", "pipes.mjPerMillibucket", (int) mjPerMillibucket).setMinValue((int) MJ_REQ_MILLIBUCKET_MIN);
        EnumRestartRequirement.WORLD.setTo(propMjPerMillibucket);

        propMjPerItem = config.get("general", "pipes.mjPerItem", (int) mjPerItem).setMinValue((int) MJ_REQ_ITEM_MIN);
        EnumRestartRequirement.WORLD.setTo(propMjPerItem);

        propBaseFlowRate = config.get("general", "pipes.baseFluidRate", baseFlowRate).setMinValue(1).setMaxValue(40);
        EnumRestartRequirement.WORLD.setTo(propBaseFlowRate);

        propLossMode = config.get("experimental", "kinesisLossMode", "lossless");
        propLossMode.setValidValues(new String[] { PowerLossMode.LOSSLESS.configName, PowerLossMode.ABSOLUTE.configName, PowerLossMode.PERCENTAGE.configName });
        EnumRestartRequirement.WORLD.setTo(propLossMode);

        MinecraftForge.EVENT_BUS.register(BCTransportConfig.class);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        if (EnumRestartRequirement.WORLD.hasBeenRestarted(restarted)) {
            mjPerMillibucket = propMjPerMillibucket.getLong();
            if (mjPerMillibucket < MJ_REQ_MILLIBUCKET_MIN) {
                mjPerMillibucket = MJ_REQ_MILLIBUCKET_MIN;
            }

            mjPerItem = propMjPerItem.getLong();
            if (mjPerItem < MJ_REQ_ITEM_MIN) {
                mjPerItem = MJ_REQ_ITEM_MIN;
            }

            baseFlowRate = MathUtil.clamp(propBaseFlowRate.getInt(), 1, 40);
            int basePowerRate = 4;

            lossMode = PowerLossMode.get(propLossMode.getString(), true);

            fluidTransfer(BCTransportPipes.cobbleFluid, baseFlowRate, 10);
            fluidTransfer(BCTransportPipes.woodFluid, baseFlowRate, 10);
            fluidTransfer(BCTransportPipes.voidFluid, baseFlowRate, 10);

            fluidTransfer(BCTransportPipes.stoneFluid, baseFlowRate * 2, 10);
            fluidTransfer(BCTransportPipes.sandstoneFluid, baseFlowRate * 2, 10);

            fluidTransfer(BCTransportPipes.clayFluid, baseFlowRate * 4, 10);
            fluidTransfer(BCTransportPipes.ironFluid, baseFlowRate * 4, 10);
            fluidTransfer(BCTransportPipes.quartzFluid, baseFlowRate * 4, 10);
            fluidTransfer(BCTransportPipes.diaWoodFluid, baseFlowRate * 4, 10);

            fluidTransfer(BCTransportPipes.diamondFluid, baseFlowRate * 8, 10);
            fluidTransfer(BCTransportPipes.goldFluid, baseFlowRate * 8, 2);

            powerTransfer(BCTransportPipes.cobblePower, basePowerRate, 16, false);
            powerTransfer(BCTransportPipes.stonePower, basePowerRate * 2, 32, false);
            powerTransfer(BCTransportPipes.woodPower, basePowerRate * 4, 128, true);
            powerTransfer(BCTransportPipes.sandstonePower, basePowerRate * 4, 32, false);
            powerTransfer(BCTransportPipes.quartzPower, basePowerRate * 8, 32, false);
            // powerTransfer(BCTransportPipes.ironPower, basePowerRate * 8, false);
            powerTransfer(BCTransportPipes.goldPower, basePowerRate * 16, 32, false);
            // powerTransfer(BCTransportPipes.diamondPower, basePowerRate * 32, false);
        }
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
    public static void onConfigChange(OnConfigChangedEvent cce) {
        if (BCModules.isBcMod(cce.getModID())) {
            EnumRestartRequirement req = EnumRestartRequirement.NONE;
            if (Loader.instance().isInState(LoaderState.AVAILABLE)) {
                // The loaders state will be LoaderState.SERVER_STARTED when we are in a world
                req = EnumRestartRequirement.WORLD;
            }
            reloadConfig(req);
        }
    }
}
