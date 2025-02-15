package buildcraft.datagen.base;

import buildcraft.api.BCModules;
import buildcraft.core.BCCoreItems;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.pipe.PipeRegistry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.concurrent.CompletableFuture;

public class BCItemTagsGenerator extends ItemTagsProvider {
    public BCItemTagsGenerator(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagsProvider, ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, blockTagsProvider, BCModules.BUILDCRAFT, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider) {
        // gear
        tag(OreDictionaryTags.GEAR_WOOD)
                .add(BCCoreItems.gearWood.get())
        ;
        tag(OreDictionaryTags.GEAR_STONE)
                .add(BCCoreItems.gearStone.get())
        ;
        tag(OreDictionaryTags.GEAR_IRON)
                .add(BCCoreItems.gearIron.get())
        ;
        tag(OreDictionaryTags.GEAR_GOLD)
                .add(BCCoreItems.gearGold.get())
        ;
        tag(OreDictionaryTags.GEAR_DIAMOND)
                .add(BCCoreItems.gearDiamond.get())
        ;
        tag(OreDictionaryTags.GEARS)
                .addTag(OreDictionaryTags.GEAR_WOOD)
                .addTag(OreDictionaryTags.GEAR_STONE)
                .addTag(OreDictionaryTags.GEAR_IRON)
                .addTag(OreDictionaryTags.GEAR_GOLD)
                .addTag(OreDictionaryTags.GEAR_DIAMOND)
        ;

        // tool
        tag(OreDictionaryTags.WRENCH)
                .add(BCCoreItems.wrench.get())
        ;

        tag(OreDictionaryTags.PAINT_BRUSH)
                .add(BCCoreItems.colourBrushMap.values().stream().map(RegistryObject::get).toArray(Item[]::new))
        ;

        // sealant
        tag(OreDictionaryTags.SEALANT)
                .addTag(Tags.Items.DYES_GREEN)
                .addTag(Tags.Items.SLIMEBALLS)
        ;

        // misc
        tag(OreDictionaryTags.WORKBENCHES_ITEM)
                .add(Items.CRAFTING_TABLE)
        ;

        tag(OreDictionaryTags.CLAY)
                .add(Items.CLAY)
        ;

        // pipe plugs
        tag(OreDictionaryTags.waterproof).add(BCTransportItems.waterproof.get());

        // pipes
        OreDictionaryTags.pipeColorTags.forEach((c, t) ->
        {
            IntrinsicTagAppender<Item> tagProvider = tag(t);
            PipeRegistry.INSTANCE.getAllRegisteredPipes().forEach(d -> tagProvider.add((Item) PipeRegistry.INSTANCE.getItemForPipe(d, c)));
        });

        tag(OreDictionaryTags.pipeStructure).add(BCTransportItems.pipeStructure.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemWood).add(BCTransportItems.pipeItemWood.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeFluidWood).add(BCTransportItems.pipeFluidWood.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipePowerWood).add(BCTransportItems.pipePowerWood.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemStone).add(BCTransportItems.pipeItemStone.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeFluidStone).add(BCTransportItems.pipeFluidStone.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipePowerStone).add(BCTransportItems.pipePowerStone.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemCobble).add(BCTransportItems.pipeItemCobble.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeFluidCobble).add(BCTransportItems.pipeFluidCobble.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipePowerCobble).add(BCTransportItems.pipePowerCobble.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemQuartz).add(BCTransportItems.pipeItemQuartz.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeFluidQuartz).add(BCTransportItems.pipeFluidQuartz.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipePowerQuartz).add(BCTransportItems.pipePowerQuartz.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemGold).add(BCTransportItems.pipeItemGold.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeFluidGold).add(BCTransportItems.pipeFluidGold.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipePowerGold).add(BCTransportItems.pipePowerGold.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemSandstone).add(BCTransportItems.pipeItemSandstone.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeFluidSandstone).add(BCTransportItems.pipeFluidSandstone.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipePowerSandstone).add(BCTransportItems.pipePowerSandstone.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemIron).add(BCTransportItems.pipeItemIron.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeFluidIron).add(BCTransportItems.pipeFluidIron.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
//        tag(OreDictTag.pipePowerIron).add(BCTransportItems.pipePowerIron.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemDiamond).add(BCTransportItems.pipeItemDiamond.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeFluidDiamond).add(BCTransportItems.pipeFluidDiamond.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
//        tag(OreDictTag.pipePowerDiamond).add(BCTransportItems.pipePowerDiamond.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemDiaWood).add(BCTransportItems.pipeItemDiaWood.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeFluidDiaWood).add(BCTransportItems.pipeFluidDiaWood.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
//        tag(OreDictTag.pipePowerDiaWood).add(BCTransportItems.pipePowerDiaWood.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemClay).add(BCTransportItems.pipeItemClay.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeFluidClay).add(BCTransportItems.pipeFluidClay.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemVoid).add(BCTransportItems.pipeItemVoid.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeFluidVoid).add(BCTransportItems.pipeFluidVoid.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemObsidian).add(BCTransportItems.pipeItemObsidian.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
//        tag(OreDictTag.pipeFluidObsidian).add(BCTransportItems.pipeFluidObsidian.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemLapis).add(BCTransportItems.pipeItemLapis.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemDaizuli).add(BCTransportItems.pipeItemDaizuli.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemEmzuli).add(BCTransportItems.pipeItemEmzuli.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));
        tag(OreDictionaryTags.pipeItemStripes).add(BCTransportItems.pipeItemStripes.values().stream().map(r -> (Item) r.get()).toArray(Item[]::new));

    }

    @Override
    public String getName() {
        return "BuildCraft Item Tags Generator";
    }
}
