package buildcraft.core;

import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumSpring;
import buildcraft.core.block.*;
import buildcraft.core.item.ItemBlockDecorated;
import buildcraft.core.item.ItemBlockSpring;
import buildcraft.core.item.ItemEngine_BC8;
import buildcraft.core.tile.*;
import buildcraft.lib.BCLib;
import buildcraft.lib.block.BlockPropertiesCreator;
import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

public class BCCoreBlocks {
    public static final RegistrationHelper HELPER = new RegistrationHelper(BCCore.MODID);

    public static final Map<EnumEngineType, RegistryObject<? extends BlockEngineBase_BC8<EnumEngineType>>> engineBlockMap = new EnumMap<>(EnumEngineType.class);

    public static RegistryObject<BlockEngine_BC8> engineWood;
    public static RegistryObject<BlockEngine_BC8> engineCreative;
    public static RegistryObject<BlockSpring> springWater;
    public static RegistryObject<BlockSpring> springOil;
    // public static RegistryObject<BlockDecoration> decorated;
    public static final Map<EnumDecoratedBlock, RegistryObject<BlockDecoration>> decoratedMap = new HashMap<>();
    public static RegistryObject<BlockMarkerVolume> markerVolume;
    public static RegistryObject<BlockMarkerPath> markerPath;
    public static RegistryObject<BlockPowerConsumerTester> powerTester;

    public static RegistryObject<BlockEntityType<TileEngineRedstone_BC8>> engineWoodTile;
    public static RegistryObject<BlockEntityType<TileEngineCreative>> engineCreativeTile;
    public static RegistryObject<BlockEntityType<TileMarkerVolume>> markerVolumeTile;
    public static RegistryObject<BlockEntityType<TileMarkerPath>> markerPathTile;
    public static RegistryObject<BlockEntityType<TilePowerConsumerTester>> powerTesterTile;

    private static final BlockBehaviour.Properties SPRING_PROPERTIES =
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable() // noDrops()
                    .sound(SoundType.STONE)
                    .randomTicks();

    // Calen: static initialize for energy module access
    static {
        springWater = HELPER.addBlockAndItem(
                "block.spring.water",
                SPRING_PROPERTIES,
                (idBC, properties) -> new BlockSpring(idBC, properties, EnumSpring.WATER),
                ItemBlockSpring::new
        );
        springOil = HELPER.addBlockAndItem(
                "block.spring.oil",
                SPRING_PROPERTIES,
                (idBC, properties) -> new BlockSpring(idBC, properties, EnumSpring.OIL),
                ItemBlockSpring::new
        );
        String registryIdDecorated = TagManager.getTag("block.decorated", TagManager.EnumTagType.REGISTRY_NAME).replace(BCCore.MODID + ":", "");
        for (EnumDecoratedBlock decoratedBlock : EnumDecoratedBlock.values()) {
            RegistryObject<BlockDecoration> reg = HELPER.addBlockAndItem(
                    "block.decorated",
                    registryIdDecorated + "_" + decoratedBlock.getSerializedName(),
                    BlockPropertiesCreator.metal(),
                    (idBC, prop) -> new BlockDecoration(idBC, prop, decoratedBlock),
                    (idBC, prop) -> new ItemBlockDecorated(idBC, prop, decoratedBlock)
            );
            decoratedMap.put(decoratedBlock, reg);
        }
        markerVolume = HELPER.addBlockAndItem("block.marker.volume", BlockPropertiesCreator.decoration().strength(0.25F).noOcclusion().noCollission(), BlockMarkerVolume::new);
        markerPath = HELPER.addBlockAndItem("block.marker.path", BlockPropertiesCreator.decoration().strength(0.25F).noOcclusion().noCollission(), BlockMarkerPath::new);
        if (BCLib.DEV) {
            powerTester = HELPER.addBlockAndItem("block.power_tester", BlockPropertiesCreator.metal(), BlockPowerConsumerTester::new);
        }

        engineWood = registerEngine(EnumEngineType.WOOD, TileEngineRedstone_BC8::new);
        engineCreative = registerEngine(EnumEngineType.CREATIVE, TileEngineCreative::new);

        markerVolumeTile = HELPER.registerTile("tile.marker.volume", TileMarkerVolume::new, markerVolume);
        markerPathTile = HELPER.registerTile("tile.marker.path", TileMarkerPath::new, markerPath);
        engineWoodTile = HELPER.registerTile("tile.engine.wood", TileEngineRedstone_BC8::new, engineWood);
        engineCreativeTile = HELPER.registerTile("tile.engine.creative", TileEngineCreative::new, engineCreative);
        if (BCLib.DEV) {
            powerTesterTile = HELPER.registerTile("tile.power_tester", TilePowerConsumerTester::new, powerTester);
        }
    }

    public static void preInit() {

    }

    public static RegistryObject<BlockEngine_BC8> registerEngine(EnumEngineType type, BiFunction<BlockPos, BlockState, ? extends TileEngineBase_BC8> engineTileConstructor) {
        RegistryObject<BlockEngine_BC8> engine = null;
        String regName = TagManager.getTag("block.engine.bc." + type.getSerializedName(), TagManager.EnumTagType.REGISTRY_NAME).replace(BCCore.MODID + ":", "");
        if (RegistryConfig.isEnabled(
                "engines",
                type.name().toLowerCase(Locale.ROOT),
                TagManager.getTag("block.engine.bc." + type.getSerializedName(), TagManager.EnumTagType.UNLOCALIZED_NAME)
        )) {
            String id = "block.engine.bc." + type.getSerializedName();
//            engine = HELPER.addBlockAndItem(id, ENGINE_PROPERTIES, (idBC, properties) -> new BlockEngine_BC8(idBC, properties, type), ItemEngine_BC8::new);
            engine = HELPER.addBlockAndItem(id,
                    BlockPropertiesCreator.metal()
                            .strength(5.0F, 10.0F)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                    , (idBC, properties) -> new BlockEngine_BC8(idBC, properties, type, engineTileConstructor), ItemEngine_BC8::new);
            engineBlockMap.put(type, engine);
        }
        return engine;
    }


}
