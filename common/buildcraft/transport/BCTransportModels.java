package buildcraft.transport;

import com.google.common.collect.ImmutableMap;

import buildcraft.lib.client.model.CustomModelLoader.ModelHolder;

public class BCTransportModels {
    public static final ModelHolder BLOCKER;
    public static final ModelHolder POWER_ADAPTER;
    public static final ModelHolder LENS;

    static {
        BLOCKER = getModel("plugs/blocker.json");
        POWER_ADAPTER = getModel("plugs/power_adapter.json");
        LENS = getModel("plugs/lens.json");
    }

    private static ModelHolder getModel(String loc) {
        return getModel(loc, false);
    }

    private static ModelHolder getModel(String loc, boolean allowTextureFallthrough) {
        return new ModelHolder("buildcrafttransport:models/" + loc, ImmutableMap.of(), allowTextureFallthrough);
    }

    public static void fmlPreInit() {
        // Nothing, just to register models
    }
}
