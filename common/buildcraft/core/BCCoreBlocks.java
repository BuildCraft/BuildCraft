package buildcraft.core;

import net.minecraft.block.material.Material;

import buildcraft.core.block.BlockDecoration;
import buildcraft.core.block.BlockEngine_BC8;
import buildcraft.core.block.BlockMarkerPath;
import buildcraft.core.block.BlockMarkerVolume;
import buildcraft.core.item.ItemBlockDecorated;
import buildcraft.core.tile.TileMarkerPath;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

public class BCCoreBlocks {
    public static BlockEngine_BC8 engine;
    public static BlockSpring spring;
    public static BlockDecoration decorated;
    public static BlockMarkerVolume markerVolume;
    public static BlockMarkerPath markerPath;

    public static void preInit() {
        markerVolume = BlockBCBase_Neptune.register(new BlockMarkerVolume(Material.CIRCUITS, "block.marker.volume"));
        markerPath = BlockBCBase_Neptune.register(new BlockMarkerPath(Material.CIRCUITS, "block.marker.path"));
        decorated = BlockBCBase_Neptune.register(new BlockDecoration("block.decorated"), ItemBlockDecorated::new);

        // engine = BlockBuildCraftBase_BC8.register(new BlockEngine_BC8(Material.IRON, "block.engine.bc"),
        // ItemEngine_BC8<EnumEngineType>::new);

        // engine.registerEngine(EnumEngineType.WOOD, TileEngineRedstone_BC8::new);
        TileBC_Neptune.registerTile(TileMarkerVolume.class, "tile.marker.volume");
        TileBC_Neptune.registerTile(TileMarkerPath.class, "tile.marker.path");
    }
}
