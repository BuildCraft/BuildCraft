package ct.buildcraft.core;

import ct.buildcraft.api.enums.EnumEngineType;
import ct.buildcraft.core.block.BlockEngine_BC8;
import ct.buildcraft.core.block.BlockMarkerPath;
import ct.buildcraft.core.block.BlockMarkerVolume;
import ct.buildcraft.core.block.BlockSpring;
import ct.buildcraft.core.blockEntity.TileEngineCreative;
import ct.buildcraft.core.blockEntity.TileEngineRedstone_BC8;
import ct.buildcraft.core.blockEntity.TileMarkerPath;
import ct.buildcraft.core.blockEntity.TileMarkerVolume;
import ct.buildcraft.energy.tile.TileEngineIron_BC8;
import ct.buildcraft.energy.tile.TileEngineStone_BC8;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCCoreBlocks {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITYS = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BCCore.MODID);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BCCore.MODID);
//    public static final RegistryObject<Block> ENGINE_RESTONE_BLOCK = BLOCKS.register("engine_redstone", () -> new BlockEngine(TileEngineRedstone::new,"redstone"));
//    public static final RegistryObject<Block> ENGINE_CREATIVE_BLOCK = BLOCKS.register("engine_creative", () -> new BlockEngine(TileEngineCreative::new,"creative"));

    
    
    public static final RegistryObject<Block> ENGINE_BC8 = BLOCKS.register("engine", () -> new BlockEngine_BC8(BlockBehaviour.Properties.of(Material.METAL)
    		.strength(25.0f).explosionResistance(10.0f).dynamicShape().requiresCorrectToolForDrops())
    		.registerEngine(EnumEngineType.WOOD, TileEngineRedstone_BC8::new)
    		.registerEngine(EnumEngineType.CREATIVE, TileEngineCreative::new)
    		.registerEngine(EnumEngineType.STONE, TileEngineStone_BC8::new)
    		.registerEngine(EnumEngineType.IRON, TileEngineIron_BC8::new));

    public static final RegistryObject<BlockSpring> SPRING = BLOCKS.register("spring", BlockSpring::new);
    public static final RegistryObject<BlockMarkerPath> MARKER_PATH = BLOCKS.register("marker_path", BlockMarkerPath::new);
    public static final RegistryObject<BlockMarkerVolume> MARKER_VOLUME = BLOCKS.register("marker_volume", BlockMarkerVolume::new);
//    public static BlockMarkerPath markerPath;
    
    
/*    public static final RegistryObject<BlockEntityType<TileEngineRedstone>> ENGINE_REDSTONE_TILE = BLOCK_ENTITYS.register("entity_engine_redstone", 
    		() -> BlockEntityType.Builder.of(TileEngineRedstone::new,ENGINE_RESTONE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEngineCreative>> ENGINE_CREATIVE_TILE = BLOCK_ENTITYS.register("entity_engine_creative", 
    		() -> BlockEntityType.Builder.of(TileEngineCreative::new,ENGINE_CREATIVE_BLOCK.get()).build(null));*/
    
    public static final RegistryObject<BlockEntityType<TileEngineRedstone_BC8>> ENGINE_REDSTONE_TILE_BC8 = BLOCK_ENTITYS.register("entity_engine_redstone", 
    		() -> BlockEntityType.Builder.of(TileEngineRedstone_BC8::new,ENGINE_BC8.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<TileEngineCreative>> ENGINE_CREATIVE_TILE_BC8 = BLOCK_ENTITYS.register("entity_engine_creative", 
    		() -> BlockEntityType.Builder.of(TileEngineCreative::new,ENGINE_BC8.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<TileMarkerPath>> MARKER_PATH_TILE_BC8 = BLOCK_ENTITYS.register("entity_marker_path", 
    		() -> BlockEntityType.Builder.of(TileMarkerPath::new,MARKER_PATH.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<TileMarkerVolume>> MARKER_VOLUME_TILE_BC8 = BLOCK_ENTITYS.register("entity_marker_volume", 
    		() -> BlockEntityType.Builder.of(TileMarkerVolume::new,MARKER_VOLUME.get()).build(null));
    
    static void registry(IEventBus m) {
        BLOCKS.register(m);
        BLOCK_ENTITYS.register(m);
    }
    
}
