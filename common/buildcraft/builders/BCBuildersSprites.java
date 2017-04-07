package buildcraft.builders;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class BCBuildersSprites {
    public static final SpriteHolder FILLING_PLANNER;

    static {
        FILLING_PLANNER = getHolder("addons/filling_planner");
    }

    private static SpriteHolder getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftbuilders:" + suffix);
    }

    public static void fmlPreInit() {
        // Nothing, just to register the sprites
    }
}
