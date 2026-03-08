//package buildcraft.datagen.base;
//
//import buildcraft.api.BCModules;
//import buildcraft.energy.generation.biome.BCBiomeRegistry;
//import buildcraft.lib.oredictionarytag.OreDictionaryTags;
//import net.minecraft.data.DataGenerator;
//import net.minecraft.data.BiomeTagsProvider;
//import net.minecraft.tags.BiomeTags;
//import net.minecraftforge.common.Tags;
//import net.minecraftforge.common.Tags.Biomes;
//import net.minecraftforge.common.data.ExistingFileHelper;
//
//public class BCBiomeTagsGenerator extends BiomeTagsProvider {
//    public BCBiomeTagsGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
//        super(generator, BCModules.BUILDCRAFT, existingFileHelper);
//    }
//
//    @Override
//    protected void addTags() {
//        // Add anything but nether, end and void biomes
//        tag(OreDictionaryTags.OIL_GEN)
//                .addTag(Tags.Biomes.IS_OVERWORLD)
//        ;
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
//        tag(Biomes.IS_OVERWORLD)
//                .add(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_DESERT)
//                .add(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_OCEAN)
//        ;
//    }
//
//    @Override
//    public String getName() {
//        return "BuildCraft Biome Tags Generator";
//    }
//}
