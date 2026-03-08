package buildcraft.datagen.base;

import buildcraft.api.BCModules;
import buildcraft.core.BCCoreBlocks;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.block.Blocks;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.nio.file.Path;

public class BCBlockTagsGenerator extends BlockTagsProvider {
    public BCBlockTagsGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, BCModules.BUILDCRAFT, existingFileHelper);
    }

    @Override
    protected void addTags() {
        // bedrock_like
        tag(BlockTags.DRAGON_IMMUNE)
                .addOptional(BCCoreBlocks.springWater.getId())
                .addOptional(BCCoreBlocks.springOil.getId())
                .addOptional(BCFactoryBlocks.tube.getId())
        ;

        tag(BlockTags.WITHER_IMMUNE)
                .addOptional(BCCoreBlocks.springWater.getId())
                .addOptional(BCCoreBlocks.springOil.getId())
                .addOptional(BCFactoryBlocks.tube.getId())
        ;

//        // pickaxe mineable
//        TagAppender<Block> mineable_with_pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE)
//                .addOptional(BCEnergyBlocks.mjDynamo.getId())
//                .addOptional(BCFactoryBlocks.autoWorkbenchItems.getId())
//                .addOptional(BCFactoryBlocks.chute.getId())
//                .addOptional(BCFactoryBlocks.distiller.getId())
//                .addOptional(BCFactoryBlocks.heatExchange.getId())
//                .addOptional(BCFactoryBlocks.tank.getId())
//                .addOptional(BCFactoryBlocks.pump.getId())
//                .addOptional(BCFactoryBlocks.miningWell.getId())
//                .addOptional(BCFactoryBlocks.waterGel.getId())
//                .addOptional(BCFactoryBlocks.floodGate.getId())
//                .addOptional(BCBuildersBlocks.quarry.getId())
//                .addOptional(BCBuildersBlocks.builder.getId())
//                .addOptional(BCBuildersBlocks.architect.getId())
//                .addOptional(BCBuildersBlocks.filler.getId())
//                .addOptional(BCBuildersBlocks.library.getId())
//                .addOptional(BCBuildersBlocks.replacer.getId())
//                .addOptional(BCSiliconBlocks.integrationTable.getId())
//                .addOptional(BCSiliconBlocks.assemblyTable.getId())
//                .addOptional(BCSiliconBlocks.advancedCraftingTable.getId())
//                .addOptional(BCSiliconBlocks.programmingTable.getId())
//                .addOptional(BCSiliconBlocks.chargingTable.getId())
//                .addOptional(BCSiliconBlocks.laser.getId())
//                .addOptional(BCRoboticsBlocks.zonePlanner.getId())
//                .addOptional(BCRoboticsBlocks.requester.getId())
//                .addOptional(BCTransportBlocks.filteredBuffer.getId());
//        BCCoreBlocks.engineBlockMap.values().stream().forEach(reg -> mineable_with_pickaxe.addOptional(reg.getId()));

        tag(OreDictionaryTags.WORKBENCHES_BLOCK)
                .addOptional(Blocks.CRAFTING_TABLE.getRegistryName())
        ;

        tag(OreDictionaryTags.SOFT)
                .addOptional(Blocks.AIR.getRegistryName())
                .addOptional(Blocks.SNOW.getRegistryName())
                .addOptional(Blocks.VINE.getRegistryName())
                .addOptional(Blocks.FIRE.getRegistryName())
        ;
    }

    @Override
    protected Path getPath(ResourceLocation id) {
        return this.generator.getOutputFolder()
                .resolve("data/" + id.getNamespace() + "/tags/blocks/" + id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "BuildCraft Block Tags Generator";
    }
}
