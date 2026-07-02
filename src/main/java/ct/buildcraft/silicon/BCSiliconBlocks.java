package ct.buildcraft.silicon;

import ct.buildcraft.api.enums.EnumLaserTableType;
import ct.buildcraft.silicon.block.BlockLaser;
import ct.buildcraft.silicon.block.BlockLaserTable;
import ct.buildcraft.silicon.tile.TileAdvancedCraftingTable;
import ct.buildcraft.silicon.tile.TileAssemblyTable;
import ct.buildcraft.silicon.tile.TileChargingTable;
import ct.buildcraft.silicon.tile.TileIntegrationTable;
import ct.buildcraft.silicon.tile.TileLaser;
import ct.buildcraft.silicon.tile.TileProgrammingTable_Neptune;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCSiliconBlocks {
	
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BCSilicon.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITYS = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BCSilicon.MODID);
    public static final RegistryObject<Block> LASER_BLOCK = BLOCKS.register("laser", BlockLaser::new);
    public static final RegistryObject<Block> ASSEMBLY_TABLE_BLOCK = BLOCKS.register("assembly_table", () -> new BlockLaserTable(EnumLaserTableType.ASSEMBLY_TABLE));
    public static final RegistryObject<Block> CHARGING_TABLE_BLOCK = BLOCKS.register("charging_table", () -> new BlockLaserTable(EnumLaserTableType.CHARGING_TABLE));
    public static final RegistryObject<Block> INTERGRATION_TABLE_BLOCK = BLOCKS.register("integration_table", () -> new BlockLaserTable(EnumLaserTableType.INTEGRATION_TABLE));
    public static final RegistryObject<Block> ADVANCED_CRAFTING_TABLE_BLOCK = BLOCKS.register("advanced_crafting_table", () -> new BlockLaserTable(EnumLaserTableType.ADVANCED_CRAFTING_TABLE));
    public static final RegistryObject<Block> PROGRAMMING_TABLE_BLOCK = BLOCKS.register("programming_table", () -> new BlockLaserTable(EnumLaserTableType.PROGRAMMING_TABLE));

    
    public static final RegistryObject<BlockEntityType<TileLaser>> LASER_TILE = BLOCK_ENTITYS.register("entity_laser", 
    		() -> BlockEntityType.Builder.of(TileLaser::new,LASER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileAssemblyTable>> ASSEMBLY_TABLE_TILE = BLOCK_ENTITYS.register("entity_assembly_table", 
    		() -> BlockEntityType.Builder.of(TileAssemblyTable::new,ASSEMBLY_TABLE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileChargingTable>> CHARGING_TABLE_TILE = BLOCK_ENTITYS.register("entity_charging_table", 
    		() -> BlockEntityType.Builder.of(TileChargingTable::new,CHARGING_TABLE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileIntegrationTable>> INTERGRATION_TABLE_TILE = BLOCK_ENTITYS.register("entity_integration_table", 
    		() -> BlockEntityType.Builder.of(TileIntegrationTable::new,INTERGRATION_TABLE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileAdvancedCraftingTable>> ADVANCED_CRAFTING_TABLE_TILE = BLOCK_ENTITYS.register("entity_advanced_crafting_table", 
    		() -> BlockEntityType.Builder.of(TileAdvancedCraftingTable::new,ADVANCED_CRAFTING_TABLE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileProgrammingTable_Neptune>> PROGRAMMING_TABLE_TILE = BLOCK_ENTITYS.register("entity_programming_table", 
    		() -> BlockEntityType.Builder.of(TileProgrammingTable_Neptune::new,PROGRAMMING_TABLE_BLOCK.get()).build(null));
    
    
    public static void registry(IEventBus b) {
    	BLOCKS.register(b);
    	BLOCK_ENTITYS.register(b);
    }
}
