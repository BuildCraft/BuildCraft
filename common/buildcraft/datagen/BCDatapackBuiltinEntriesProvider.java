package buildcraft.datagen;

import buildcraft.api.BCModules;
import buildcraft.builders.snapshot.FakeWorld;
import buildcraft.energy.generation.biome.BCBiomes;
import buildcraft.energy.generation.structure.OilGenerator;
import buildcraft.energy.generation.structure.OilStructureFeature;
import buildcraft.energy.generation.structure.OilStructureRegistry;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BCDatapackBuiltinEntriesProvider extends DatapackBuiltinEntriesProvider {
    public static final DimensionType DIMENSION_TYPE = new DimensionType(
            OptionalLong.empty(), // fixedTime
            true, // hasSkylight
            false, // hasCeiling
            false, // ultraWarm
            true, // natural
            1.0D, // coordinateScale
//            false, // createDragonFight
//            false, // piglinSafe
            true, // bedWorks
            false, // respawnAnchorWorks
//            true, // hasRaids
            -64, // minY
            384, // height
            384, // logicalHeight
            BlockTags.INFINIBURN_OVERWORLD, // infiniburn
            BuiltinDimensionTypes.OVERWORLD_EFFECTS, // effectsLocation
            0.0F, // ambientLight
            new DimensionType.MonsterSettings(true, false, UniformInt.of(0, 7), 0)
    );
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.STRUCTURE, BCDatapackBuiltinEntriesProvider::initDatagenStructures)
            .add(Registries.STRUCTURE_SET, BCDatapackBuiltinEntriesProvider::initDatagenStructureSets)
            .add(Registries.BIOME, BCBiomes::initBiome)
            .add(Registries.DIMENSION_TYPE, ctx -> ctx.register(FakeWorld.DIMENSION_TYPE_KEY, DIMENSION_TYPE));


    public BCDatapackBuiltinEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, BUILDER, Arrays.stream(BCModules.values()).map(m -> BCModules.BUILDCRAFT + m.lowerCaseName).collect(Collectors.toSet()));
    }

    public static Holder<Structure> CONFIGURED_INSTANCE_OIL_STRUCTURE;

    public static void initDatagenStructures(BootstapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderSet.Named<Biome> biomeHolderSet = biomes.getOrThrow(OreDictionaryTags.OIL_GEN);
//        MappedRegistry<Structure> mappedRegistry = ForgeRegistries.BIOMES.getSlaveMap();
//        mappedRegistry.getOrCreateTag(OreDictionaryTags.OIL_GEN);
        CONFIGURED_INSTANCE_OIL_STRUCTURE = context.register(
                ResourceKey.create(
                        Registries.STRUCTURE,
                        OilStructureRegistry.STRUCTURE_ID
                ),
                new OilStructureFeature(
                        new Structure.StructureSettings(
                                biomeHolderSet,
                                Map.of(),
                                GenerationStep.Decoration.FLUID_SPRINGS,
                                TerrainAdjustment.NONE)
                )
        );
    }

    public static void initDatagenStructureSets(BootstapContext<StructureSet> context) {
//        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
//        Holder.Reference<Structure> oil = structures.getOrThrow(STRUCTURE_KEY);

//        ForgeRegistry<Structure> registry = RegistryManager.ACTIVE.getRegistry(Registries.STRUCTURE);
//        Holder<Structure> oil = registry.getHolder(STRUCTURE_KEY).get();

        // 1.18.2: the salt should not be negative...
        context.register(
                ResourceKey.create(
                        Registries.STRUCTURE_SET,
                        OilStructureRegistry.STRUCTURE_ID
                ),
                new StructureSet(
                        List.of(StructureSet.entry(CONFIGURED_INSTANCE_OIL_STRUCTURE)),
                        new RandomSpreadStructurePlacement(1, 0, RandomSpreadType.LINEAR, Math.abs((int) (OilGenerator.MAGIC_GEN_NUMBER >> 32)))
                )
        );
    }
}
