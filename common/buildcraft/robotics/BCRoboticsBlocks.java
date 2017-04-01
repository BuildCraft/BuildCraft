package buildcraft.robotics;

import net.minecraft.block.material.Material;

import buildcraft.lib.BCLib;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.robotics.block.BlockZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;

public class BCRoboticsBlocks {
    public static BlockZonePlanner zonePlanner;

    public static void preInit() {
        if (BCLib.DEVELOPER) {
            zonePlanner = BlockBCBase_Neptune.register(new BlockZonePlanner(Material.ROCK, "block.zone_planner"));

            TileBC_Neptune.registerTile(TileZonePlanner.class, "tile.zone_planner");
        }
    }
}
