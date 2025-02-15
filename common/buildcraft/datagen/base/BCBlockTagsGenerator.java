package buildcraft.datagen.base;

import buildcraft.api.BCModules;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.core.BCCoreBlocks;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.transport.BCTransportBlocks;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class BCBlockTagsGenerator extends BlockTagsProvider {
    public BCBlockTagsGenerator(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, BCModules.BUILDCRAFT, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider) {
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
                        BCSiliconBlocks.laser.get(),
                        BCSiliconBlocks.chargingTable.get(),
                        BCTransportBlocks.filteredBuffer.get()
                )
        ;

        tag(OreDictionaryTags.WORKBENCHES_BLOCK)
                .add(Blocks.CRAFTING_TABLE)
        ;
    }

    @Override
    protected Path getPath(ResourceLocation id) {
        return this.pathProvider.json(ResourceLocation.tryBuild(id.getNamespace(), "tags/blocks/" + id.getPath() + ".json"));
    }

    @Override
    public String getName() {
        return "BuildCraft Block Tags Generator";
    }
}

