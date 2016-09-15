package buildcraft.silicon;

import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.block.BlockLaserTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileLaser;
import net.minecraft.block.material.Material;

public class BCSiliconBlocks {
    public static BlockLaser laser;
    public static BlockLaserTable assemblyTable;
    public static BlockLaserTable integrationTable;

    public static void preInit() {
        laser = BlockBCBase_Neptune.register(new BlockLaser(Material.ROCK, "block.laser"));
        assemblyTable = BlockBCBase_Neptune.register(new BlockLaserTable(EnumLaserTableType.ASSEMBLY_TABLE, Material.ROCK, "block.assembly_table"));
        integrationTable = BlockBCBase_Neptune.register(new BlockLaserTable(EnumLaserTableType.INTEGRATION_TABLE, Material.ROCK, "block.integration_table"));

        TileBC_Neptune.registerTile(TileLaser.class, "tile.laser");
        TileBC_Neptune.registerTile(TileAssemblyTable.class, "tile.assembly_table");
        TileBC_Neptune.registerTile(TileIntegrationTable.class, "tile.integration_table");
    }
}
