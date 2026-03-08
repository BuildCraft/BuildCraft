package buildcraft.energy.generation.structure;

import buildcraft.api.core.BCLog;
import buildcraft.energy.BCEnergy;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

public class OilStructureRegistry {
    public static String STRUCTURE_OIL_SPOUT = "oil_spout";
    public static final ResourceLocation STRUCTURE_ID = new ResourceLocation(BCEnergy.MODID, STRUCTURE_OIL_SPOUT);

    public static final StructureFeature<OilFeatureConfiguration> STRUCTURE_FEATURE =
            new OilStructureFeature(
                    OilFeatureConfiguration.CODEC
            );
    public static final ResourceKey<StructureSet> STRUCTURE_SET_KEY =
            ResourceKey.create(
                    Registry.STRUCTURE_SET_REGISTRY,
                    STRUCTURE_ID
            );
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> STRUCTURE_FEATURE_KEY =
            ResourceKey.create(
                    Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY,
                    STRUCTURE_ID
            );
    public static final StructurePieceType STRUCTURE_PIECE_TYPE = StructurePieceType.setPieceId(
            OilStructure::deserialize,
            STRUCTURE_ID.toString()
    );

    public static List<PlacementModifier> modifiers(int p_195234_) {
        return List.of(
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_TOP_SOLID,
                CountPlacement.of(p_195234_),
                BiomeFilter.biome()
        );
    }

    public static Holder<ConfiguredStructureFeature<?, ?>> CONFIGURED_INSTANCE_OIL_STRUCTURE;

    public static void register(RegistryEvent.Register<StructureFeature<?>> event) {
        IForgeRegistry<StructureFeature<?>> registry = event.getRegistry();
        if (BCEnergyConfig.enableOilGeneration) {
            registerStructure(
                    registry,
                    STRUCTURE_ID,
                    OilStructureRegistry.STRUCTURE_FEATURE,
                    Decoration.FLUID_SPRINGS
            );

            CONFIGURED_INSTANCE_OIL_STRUCTURE = StructureFeatures.register(
                    OilStructureRegistry.STRUCTURE_FEATURE_KEY,
                    OilStructureRegistry.STRUCTURE_FEATURE.configured(
                            OilFeatureConfiguration.INSTANCE,
                            OreDictionaryTags.OIL_GEN
                    )
            );

            // 1.18.2: the salt should not be negative...
            StructureSets.register(
                    OilStructureRegistry.STRUCTURE_SET_KEY,
                    new StructureSet(
                            List.of(StructureSet.entry(CONFIGURED_INSTANCE_OIL_STRUCTURE)),
                            new RandomSpreadStructurePlacement(1, 0, RandomSpreadType.LINEAR, Math.abs((int) (OilGenerator.MAGIC_GEN_NUMBER >> 32)))
                    )
            );
            BCLog.logger.info("[energy.oilgen] Registered the oil spout generator");
        } else {
            BCLog.logger.info("[energy.oilgen] Not registering the oil spout generator, as it has been disabled by the config file.");
        }
    }

    private static <F extends StructureFeature<?>> void registerStructure(
            IForgeRegistry<StructureFeature<?>> registry,
            ResourceLocation id,
            F structure,
            Decoration stage
    ) {
        StructureFeature.STEP.put(structure, stage);
        structure.setRegistryName(id);
        registry.register(structure);
    }
}
