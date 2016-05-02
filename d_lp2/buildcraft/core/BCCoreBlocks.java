package buildcraft.core;

import net.minecraft.block.material.Material;

import buildcraft.core.block.BlockEngine_BC8;
import buildcraft.core.block.BlockMarkerPath;
import buildcraft.core.block.BlockMarkerVolume;
import buildcraft.core.tile.TileMarkerPath;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.block.BlockBuildCraftBase_BC8;
import buildcraft.lib.tile.TileBuildCraft_BC8;

public class BCCoreBlocks {
    public static BlockEngine_BC8 engine;
    public static BlockSpring spring;
    public static BlockDecoration decorated;
    public static BlockMarkerVolume markerVolume;
    public static BlockMarkerPath markerPath;

    public static void preInit() {
        markerVolume = BlockBuildCraftBase_BC8.register(new BlockMarkerVolume(Material.CIRCUITS, "block.marker.volume"));
        markerPath = BlockBuildCraftBase_BC8.register(new BlockMarkerPath(Material.CIRCUITS, "block.marker.path"));

        // engine = BlockBuildCraftBase_BC8.register(new BlockEngine_BC8(Material.IRON, "block.engine.bc"),
        // ItemEngine_BC8<EnumEngineType>::new);

        // engine.registerEngine(EnumEngineType.WOOD, TileEngineRedstone_BC8::new);
        TileBuildCraft_BC8.registerTile(TileMarkerVolume.class, "tile.marker.volume");
        TileBuildCraft_BC8.registerTile(TileMarkerPath.class, "tile.marker.path");
    }
}
