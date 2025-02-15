package buildcraft.silicon;

import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.lib.BCLib;
import buildcraft.lib.block.BlockPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.block.BlockLaserTable;
import buildcraft.silicon.tile.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

public class BCSiliconBlocks {
    private static final RegistrationHelper HELPER = new RegistrationHelper(BCSilicon.MODID);

    public static RegistryObject<BlockLaser> laser;
    public static RegistryObject<BlockLaserTable> assemblyTable;
    public static RegistryObject<BlockLaserTable> advancedCraftingTable;
    public static RegistryObject<BlockLaserTable> integrationTable;
    public static RegistryObject<BlockLaserTable> chargingTable;
    public static RegistryObject<BlockLaserTable> programmingTable;

    public static RegistryObject<BlockEntityType<TileLaser>> laserTile;
    public static RegistryObject<BlockEntityType<TileAssemblyTable>> assemblyTableTile;
    public static RegistryObject<BlockEntityType<TileAdvancedCraftingTable>> advancedCraftingTableTile;
    public static RegistryObject<BlockEntityType<TileIntegrationTable>> integrationTableTile;
    public static RegistryObject<BlockEntityType<TileChargingTable>> chargingTableTile;
    public static RegistryObject<BlockEntityType<TileProgrammingTable_Neptune>> programmingTableTile;

    public static void preInit() {
        laser = HELPER.addBlockAndItem("block.laser", BlockPropertiesCreator.metal().lightLevel((state) -> 0).noOcclusion(), BlockLaser::new);
        assemblyTable = createLaserTable(EnumLaserTableType.ASSEMBLY_TABLE, "block.assembly_table");
        advancedCraftingTable = createLaserTable(EnumLaserTableType.ADVANCED_CRAFTING_TABLE, "block.advanced_crafting_table");
        integrationTable = createLaserTable(EnumLaserTableType.INTEGRATION_TABLE, "block.integration_table");
        if (BCLib.DEV) {
            chargingTable = createLaserTable(EnumLaserTableType.CHARGING_TABLE, "block.charging_table");
            programmingTable = createLaserTable(EnumLaserTableType.PROGRAMMING_TABLE, "block.programming_table");
        }

        laserTile = HELPER.registerTile("tile.laser", TileLaser::new, laser);
        assemblyTableTile = HELPER.registerTile("tile.assembly_table", TileAssemblyTable::new, assemblyTable);
        advancedCraftingTableTile = HELPER.registerTile("tile.advanced_crafting_table", TileAdvancedCraftingTable::new, advancedCraftingTable);
        integrationTableTile = HELPER.registerTile("tile.integration_table", TileIntegrationTable::new, integrationTable);
        if (BCLib.DEV) {
            chargingTableTile = HELPER.registerTile("tile.charging_table", TileChargingTable::new, chargingTable);
            programmingTableTile = HELPER.registerTile("tile.programming_table", TileProgrammingTable_Neptune::new, programmingTable);
        }
    }

    private static RegistryObject<BlockLaserTable> createLaserTable(EnumLaserTableType type, String tagId) {
        return HELPER.addBlockAndItem(tagId, BlockPropertiesCreator.metal().lightLevel((state) -> 0).noOcclusion(), (idBC, properties) -> new BlockLaserTable(idBC, properties, type));
    }
}
