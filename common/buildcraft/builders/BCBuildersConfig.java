package buildcraft.builders;

import net.minecraftforge.common.config.Property;

import buildcraft.core.BCCoreConfig;
import buildcraft.lib.config.EnumRestartRequirement;

public class BCBuildersConfig {
    /** Blueprints that save larger than this are stored externally, smaller ones are stored directly in the item. */
    public static int bptStoreExternalThreshold = 20_000;

    /** The minimum height that all quarry frames must be. */
    public static int quarryFrameMinHeight = 4;

    private static Property propBptStoreExternalThreshold;
    private static Property propQuarryFrameMinHeight;

    public static void preInit() {
        EnumRestartRequirement none = EnumRestartRequirement.NONE;
        EnumRestartRequirement game = EnumRestartRequirement.GAME;

        propBptStoreExternalThreshold = BCCoreConfig.config.get("general", "bptStoreExternalThreshold", 20_000);
        none.setTo(propBptStoreExternalThreshold);

        propQuarryFrameMinHeight = BCCoreConfig.config.get("balance", "", 4);
        propQuarryFrameMinHeight.setComment("The minimum height that all quarry frames must be");
        propQuarryFrameMinHeight.setMinValue(2);
        none.setTo(propQuarryFrameMinHeight);

        reloadConfig(EnumRestartRequirement.GAME);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        bptStoreExternalThreshold = propBptStoreExternalThreshold.getInt();
        quarryFrameMinHeight = propQuarryFrameMinHeight.getInt();
    }
}
