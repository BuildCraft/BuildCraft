package buildcraft.builders;

import net.minecraftforge.common.config.Property;

import buildcraft.core.BCCoreConfig;
import buildcraft.lib.config.EnumRestartRequirement;

public class BCBuildersConfig {
    /** Blueprints that save larger than this are stored externally, smaller ones are stored directly in the item. */
    public static int bptStoreExternalThreshold = 20_000;

    private static Property propBptStoreExternalThreshold;

    public static void preInit() {
        propBptStoreExternalThreshold = BCCoreConfig.config.get("general", "bptStoreExternalThreshold", 20_000);

        reloadConfig(EnumRestartRequirement.GAME);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        bptStoreExternalThreshold = propBptStoreExternalThreshold.getInt();
    }
}
