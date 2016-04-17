package buildcraft.core;

import buildcraft.core.block.BlockEngine_BC8;

public class BCCoreBlocks {
    public static BlockEngine_BC8 engine;
    public static BlockSpring spring;
    public static BlockDecoration decorated;
    public static BlockMarker marker;
    public static BlockPathMarker markerPath;

    public static void preInit() {
        // engine = BlockBuildCraftBase_BC8.register(new BlockEngine_BC8(Material.IRON, "block.engine.bc"),
        // ItemEngine_BC8<EnumEngineType>::new);

        // engine.registerEngine(EnumEngineType.WOOD, TileEngineRedstone_BC8::new);
    }
}
