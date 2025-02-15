package buildcraft.datagen.base;

import buildcraft.api.BCModules;
import buildcraft.energy.generation.biome.BCBiomeRegistry;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraftforge.common.Tags.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class BCBiomeTagsGenerator extends BiomeTagsProvider {
    public BCBiomeTagsGenerator(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, BCModules.BUILDCRAFT, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider) {
        // Add anything but nether, end and void biomes
        tag(OreDictionaryTags.OIL_GEN)
                .addTag(BiomeTags.IS_OVERWORLD)
        ;
//        tag(Biomes.IS_HOT)
//                .add(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_DESERT)
//        ;
//        tag(Biomes.IS_DRY)
//                .add(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_DESERT)
//        ;
//        tag(Biomes.IS_SANDY)
//                .add(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_DESERT)
//        ;
//        tag(BiomeTags.IS_OCEAN)
//                .add(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_OCEAN)
//        ;
//        tag(BiomeTags.IS_OVERWORLD)
//                .add(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_DESERT)
//                .add(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_OCEAN)
//        ;
        tag(Biomes.IS_HOT)
                .addOptional(BCBiomeRegistry.RL_BIOME_OIL_DESERT)
        ;
        tag(Biomes.IS_DRY)
                .addOptional(BCBiomeRegistry.RL_BIOME_OIL_DESERT)
        ;
        tag(Biomes.IS_SANDY)
                .addOptional(BCBiomeRegistry.RL_BIOME_OIL_DESERT)
        ;
        tag(BiomeTags.IS_OCEAN)
                .addOptional(BCBiomeRegistry.RL_BIOME_OIL_OCEAN)
        ;
        tag(BiomeTags.IS_OVERWORLD)
                .addOptional(BCBiomeRegistry.RL_BIOME_OIL_DESERT)
                .addOptional(BCBiomeRegistry.RL_BIOME_OIL_OCEAN)
        ;
    }

    @Override
    public String getName() {
        return "BuildCraft Biome Tags Generator";
    }
}
