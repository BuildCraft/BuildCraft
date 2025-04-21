package buildcraft.datagen;

import buildcraft.core.BCCore;
import buildcraft.datagen.base.*;
import buildcraft.datagen.builders.BuildersAdvancementGenerator;
import buildcraft.datagen.builders.BuildersBlockStateGenerator;
import buildcraft.datagen.builders.BuildersCraftingRecipeGenerator;
import buildcraft.datagen.builders.BuildersItemModelGenerator;
import buildcraft.datagen.core.CoreAdvancementGenerator;
import buildcraft.datagen.core.CoreBlockStateGenerator;
import buildcraft.datagen.core.CoreCraftingRecipeGenerator;
import buildcraft.datagen.core.CoreItemModelGenerator;
import buildcraft.datagen.energy.*;
import buildcraft.datagen.factory.FactoryAdvancementGenerator;
import buildcraft.datagen.factory.FactoryBlockStateGenerator;
import buildcraft.datagen.factory.FactoryCraftingRecipeGenerator;
import buildcraft.datagen.factory.FactoryItemModelGenerator;
import buildcraft.datagen.lib.LibCraftingRecipeGenerator;
import buildcraft.datagen.lib.LibItemModelProvider;
import buildcraft.datagen.robotics.*;
import buildcraft.datagen.silicon.*;
import buildcraft.datagen.transport.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = BCCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BCDataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // Oil Texture
        generator.addProvider(new EnergyOilTextureGenerator(generator, existingFileHelper));

//        // frozen fluid texture
//        generator.addProvider(new FrozenFluidTextureProvider(generator, existingFileHelper));

        // Tags
        BlockTagsProvider blockTagsProvider = new BCBlockTagsGenerator(generator, existingFileHelper);
        generator.addProvider(blockTagsProvider);
        generator.addProvider(new BCItemTagsGenerator(generator, existingFileHelper, blockTagsProvider));
        generator.addProvider(new BCFluidTagsGenerator(generator, existingFileHelper));
        generator.addProvider(new BCBiomeTagsGenerator(generator, existingFileHelper));

        // Crafting Recipes
        generator.addProvider(new BuildersCraftingRecipeGenerator(generator));
        generator.addProvider(new CoreCraftingRecipeGenerator(generator));
        generator.addProvider(new EnergyCraftingRecipeGenerator(generator));
        generator.addProvider(new FactoryCraftingRecipeGenerator(generator));
        generator.addProvider(new LibCraftingRecipeGenerator(generator));
        generator.addProvider(new SiliconCraftingRecipeGenerator(generator));
        generator.addProvider(new TransportCraftingRecipeGenerator(generator));
        generator.addProvider(new RoboticsCraftingRecipeGenerator(generator));
        // Mod Recipes
        generator.addProvider(new SiliconFacadeSwapRecipeGenerator(generator));
        generator.addProvider(new EnergyOilRecipeGenerator(generator, existingFileHelper));
        generator.addProvider(new SiliconAssemblyRecipeGenerator(generator, existingFileHelper));
        generator.addProvider(new TransportAssemblyRecipeGenerator(generator, existingFileHelper));
        generator.addProvider(new RoboticsIntegrationRecipeGenerator(generator, existingFileHelper));
        generator.addProvider(new RoboticsProgrammingRecipeGenerator(generator, existingFileHelper));

        // Advancement
        generator.addProvider(new CoreAdvancementGenerator(generator, existingFileHelper));
        generator.addProvider(new EnergyAdvancementGenerator(generator, existingFileHelper));
        generator.addProvider(new FactoryAdvancementGenerator(generator, existingFileHelper));
        generator.addProvider(new SiliconAdvancementGenerator(generator, existingFileHelper));
        generator.addProvider(new TransportAdvancementGenerator(generator, existingFileHelper));
        generator.addProvider(new BuildersAdvancementGenerator(generator, existingFileHelper));

        // Loot Table
        generator.addProvider(new BCLootGenerator(generator));

        // BlockState and Block Model
        generator.addProvider(new CoreBlockStateGenerator(generator, existingFileHelper));
        generator.addProvider(new BuildersBlockStateGenerator(generator, existingFileHelper));
        generator.addProvider(new EnergyBlockStateGenerator(generator, existingFileHelper));
        generator.addProvider(new FactoryBlockStateGenerator(generator, existingFileHelper));
        generator.addProvider(new SiliconBlockStateGenerator(generator, existingFileHelper));
        generator.addProvider(new TransportBlockStateGenerator(generator, existingFileHelper));
        generator.addProvider(new RoboticsBlockStateGenerator(generator, existingFileHelper));

        // Item Model
        generator.addProvider(new EnergyOilBucketModelGenerator(generator, existingFileHelper));

        generator.addProvider(new CoreItemModelGenerator(generator, existingFileHelper));
        generator.addProvider(new EnergyItemModelGenerator(generator, existingFileHelper));
        generator.addProvider(new FactoryItemModelGenerator(generator, existingFileHelper));
        generator.addProvider(new BuildersItemModelGenerator(generator, existingFileHelper));
        generator.addProvider(new SiliconItemModelGenerator(generator, existingFileHelper));
        generator.addProvider(new TransportItemModelGenerator(generator, existingFileHelper));
        generator.addProvider(new LibItemModelProvider(generator, existingFileHelper));
        generator.addProvider(new RoboticsItemModelGenerator(generator, existingFileHelper));
    }
}
