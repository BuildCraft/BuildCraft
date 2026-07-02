package ct.buildcraft.factory;

import ct.buildcraft.factory.block.BlockAutoWorkbenchItems;
import ct.buildcraft.factory.block.BlockChute;
import ct.buildcraft.factory.block.BlockDistiller;
import ct.buildcraft.factory.block.BlockFloodGate;
import ct.buildcraft.factory.block.BlockHeatExchange;
import ct.buildcraft.factory.block.BlockMiningWell;
import ct.buildcraft.factory.block.BlockPump;
import ct.buildcraft.factory.block.BlockTank;
import ct.buildcraft.factory.block.BlockTube;
import ct.buildcraft.factory.block.BlockWaterGel;
import ct.buildcraft.factory.tile.TileAutoWorkbenchItems;
import ct.buildcraft.factory.tile.TileChute;
import ct.buildcraft.factory.tile.TileDistiller;
import ct.buildcraft.factory.tile.TileDistiller_BC8;
import ct.buildcraft.factory.tile.TileFloodGate;
import ct.buildcraft.factory.tile.TileHeatExchange;
import ct.buildcraft.factory.tile.TileMiningWell;
import ct.buildcraft.factory.tile.TilePump;
import ct.buildcraft.factory.tile.TileTank;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCFactoryBlocks {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITYS = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,BCFactory.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BCFactory.MODID);
    public static final RegistryObject<Block> PUMP_BLOCK = BLOCKS.register("pump", BlockPump::new);
    public static final RegistryObject<Block> TANK_BLOCK = BLOCKS.register("tank", BlockTank::new);
    public static final RegistryObject<Block> CHUTE_BLOCK = BLOCKS.register("chute", BlockChute::new);
    public static final RegistryObject<Block> FLOOD_GATE_BLOCK = BLOCKS.register("flood_gate",BlockFloodGate::new);
    public static final RegistryObject<Block> TUBE_BLOCK = BLOCKS.register("tube", BlockTube::new);
    public static final RegistryObject<Block> MINING_WELL_BLOCK = BLOCKS.register("mining_well",BlockMiningWell::new);
    public static final RegistryObject<Block> DISTILLER_BLOCK = BLOCKS.register("distiller", BlockDistiller::new);
    public static final RegistryObject<Block> HEATEXCHANGE_BLOCK = BLOCKS.register("heat_exchange", BlockHeatExchange::new);
    public static final RegistryObject<Block> WATER_GEL = BLOCKS.register("water_gel", BlockWaterGel::new);
    public static final RegistryObject<Block> AUTO_BENCH_BLOCK = BLOCKS.register("autoworkbench_item", BlockAutoWorkbenchItems::new);

    
    public static final RegistryObject<BlockEntityType<TileTank>> ENTITYBLOCKTANK = BLOCK_ENTITYS.register("entity_tank", 
    		() -> BlockEntityType.Builder.of(TileTank::new,TANK_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TilePump>> ENTITYBLOCKPUMP = BLOCK_ENTITYS.register("entity_pump",
    		() -> BlockEntityType.Builder.of(TilePump::new,PUMP_BLOCK.get()).build(null));
//    public static final RegistryObject<BlockEntityType<EntityBlockTube>> ENTITYBLOCKTUBE = BLOCK_ENTITYS.register("entity_tube",
//    		() -> BlockEntityType.Builder.of(EntityBlockTube::new,TUBE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileFloodGate>> ENTITYBLOCKFLOODGATE = BLOCK_ENTITYS.register("entity_flood_gate",
    		() -> BlockEntityType.Builder.of(TileFloodGate::new,FLOOD_GATE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileMiningWell>> ENTITYBLOCKMININGWELL = BLOCK_ENTITYS.register("entity_mining_well",
    		() -> BlockEntityType.Builder.of(TileMiningWell::new,MINING_WELL_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileDistiller_BC8>> ENTITYBLOCKDISTILLER = BLOCK_ENTITYS.register("entity_distiller", 
    		() -> BlockEntityType.Builder.of(TileDistiller_BC8::new, DISTILLER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileHeatExchange>> ENTITYBLOCKHEATEXCHANGE = BLOCK_ENTITYS.register("entity_heat_exchange", 
    		() -> BlockEntityType.Builder.of(TileHeatExchange::new, HEATEXCHANGE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileAutoWorkbenchItems>> ENTITYBLOCKAUTOBENCH = BLOCK_ENTITYS.register("entity_autoworkbench_item", 
    		() -> BlockEntityType.Builder.of(TileAutoWorkbenchItems::new, AUTO_BENCH_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileChute>> ENTITYBLOCKCHUTE = BLOCK_ENTITYS.register("entity_chute", 
    		() -> BlockEntityType.Builder.of(TileChute::new, CHUTE_BLOCK.get()).build(null));
    
    
    
	
	static void registry(IEventBus bus){
		BLOCKS.register(bus);
		BLOCK_ENTITYS.register(bus);
	}
}
