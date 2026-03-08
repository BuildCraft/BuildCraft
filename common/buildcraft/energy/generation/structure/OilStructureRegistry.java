package buildcraft.energy.generation.structure;

import buildcraft.api.core.BCLog;
import buildcraft.energy.BCEnergy;
import buildcraft.energy.BCEnergyConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureFeatures;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class OilStructureRegistry {
    public static String STRUCTURE_OIL_SPOUT = "oil_spout";
    public static final ResourceLocation STRUCTURE_ID = new ResourceLocation(BCEnergy.MODID, STRUCTURE_OIL_SPOUT);

    public static final Structure<OilFeatureConfiguration> STRUCTURE_FEATURE =
            new OilStructureFeature(
                    OilFeatureConfiguration.CODEC
            );
    public static final StructureFeature<OilFeatureConfiguration, ? extends Structure<OilFeatureConfiguration>> CONFIGURED_STRUCTURE_FEATURE =
            STRUCTURE_FEATURE.configured(OilFeatureConfiguration.INSTANCE);

    public static final IStructurePieceType STRUCTURE_PIECE_TYPE = IStructurePieceType.setPieceId(
            OilStructure::deserialize,
            STRUCTURE_ID.toString()
    );

    public static StructureFeature<OilFeatureConfiguration, ?> REGISTERED_CONFIGURED_STRUCTURE_FEATURE;

    public static void register(RegistryEvent.Register<Structure<?>> event) {
        IForgeRegistry<Structure<?>> registry = event.getRegistry();
        if (BCEnergyConfig.enableOilGeneration) {
            registerStructure(
                    registry,
                    STRUCTURE_ID,
                    OilStructureRegistry.STRUCTURE_FEATURE,
                    Decoration.VEGETAL_DECORATION
            );

            REGISTERED_CONFIGURED_STRUCTURE_FEATURE = StructureFeatures.register(
                    STRUCTURE_ID.toString(),
                    CONFIGURED_STRUCTURE_FEATURE
            );
            WorldGenRegistries.NOISE_GENERATOR_SETTINGS.get(DimensionSettings.OVERWORLD).structureSettings().structureConfig().put(STRUCTURE_FEATURE, new StructureSeparationSettings(1, 0, Math.abs((int) (OilGenerator.MAGIC_GEN_NUMBER >> 32))));

            BCLog.logger.info("[energy.oilgen] Registered the oil spout generator");
        } else {
            BCLog.logger.info("[energy.oilgen] Not registering the oil spout generator, as it has been disabled by the config file.");
        }
    }

    private static <F extends Structure<?>> void registerStructure(
            IForgeRegistry<Structure<?>> registry,
            ResourceLocation id,
            F structure,
            Decoration stage
    ) {
        Structure.STRUCTURES_REGISTRY.put(id.toString(), structure);
        Structure.STEP.put(structure, stage);
        structure.setRegistryName(id);
        registry.register(structure);
    }
}
