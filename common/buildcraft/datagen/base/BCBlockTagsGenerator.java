package buildcraft.datagen.base;

import buildcraft.api.BCModules;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.robotics.BCRoboticsBlocks;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.transport.BCTransportBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.nio.file.Path;

public class BCBlockTagsGenerator extends BlockTagsProvider {
    public BCBlockTagsGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, BCModules.BUILDCRAFT, existingFileHelper);
    }

    @Override
    protected void addTags() {
        // bedrock_like
        tag(BlockTags.DRAGON_IMMUNE)
                .add(BCCoreBlocks.springWater.get())
                .add(BCCoreBlocks.springOil.get())
                .add(BCFactoryBlocks.tube.get())
        ;

        tag(BlockTags.WITHER_IMMUNE)
                .add(BCCoreBlocks.springWater.get())
                .add(BCCoreBlocks.springOil.get())
                .add(BCFactoryBlocks.tube.get())
        ;

        // pickaxe mineable
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        BCCoreBlocks.engineBlockMap.values().stream().map(RegistryObject::get).toArray(Block[]::new)
                )
                .add(
                        BCEnergyBlocks.mjDynamo.get(),
                        BCFactoryBlocks.autoWorkbenchItems.get(),
                        BCFactoryBlocks.chute.get(),
                        BCFactoryBlocks.distiller.get(),
                        BCFactoryBlocks.heatExchange.get(),
                        BCFactoryBlocks.tank.get(),
                        BCFactoryBlocks.pump.get(),
                        BCFactoryBlocks.miningWell.get(),
                        BCFactoryBlocks.waterGel.get(),
                        BCFactoryBlocks.floodGate.get(),
                        BCBuildersBlocks.quarry.get(),
                        BCBuildersBlocks.builder.get(),
                        BCBuildersBlocks.architect.get(),
                        BCBuildersBlocks.filler.get(),
                        BCBuildersBlocks.library.get(),
                        BCBuildersBlocks.replacer.get(),
                        BCSiliconBlocks.integrationTable.get(),
                        BCSiliconBlocks.assemblyTable.get(),
                        BCSiliconBlocks.advancedCraftingTable.get(),
                        BCSiliconBlocks.programmingTable.get(),
                        BCSiliconBlocks.chargingTable.get(),
                        BCSiliconBlocks.laser.get(),
                        BCRoboticsBlocks.zonePlanner.get(),
                        BCRoboticsBlocks.requester.get(),
                        BCTransportBlocks.filteredBuffer.get()
                )
        ;

        tag(OreDictionaryTags.WORKBENCHES_BLOCK)
                .add(Blocks.CRAFTING_TABLE)
        ;

        tag(OreDictionaryTags.SOFT)
                .add(Blocks.AIR)
                .add(Blocks.SNOW)
                .add(Blocks.VINE)
                .add(Blocks.FIRE)
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

