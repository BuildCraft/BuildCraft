package buildcraft.datagen.base;

import buildcraft.api.BCModules;
import buildcraft.core.BCCoreItems;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.pipe.PipeRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.stream.Stream;

public class BCItemTagsGenerator extends ItemTagsProvider {
    public BCItemTagsGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper, BlockTagsProvider blockTagsProvider) {
        super(generator, blockTagsProvider, BCModules.BUILDCRAFT, existingFileHelper);
    }

    @Override
    protected void addTags() {
        // gear
        tag(OreDictionaryTags.GEAR_WOOD)
                .addOptional(BCCoreItems.gearWood.getId())
        ;
        tag(OreDictionaryTags.GEAR_STONE)
                .addOptional(BCCoreItems.gearStone.getId())
        ;
        tag(OreDictionaryTags.GEAR_IRON)
                .addOptional(BCCoreItems.gearIron.getId())
        ;
        tag(OreDictionaryTags.GEAR_GOLD)
                .addOptional(BCCoreItems.gearGold.getId())
        ;
        tag(OreDictionaryTags.GEAR_DIAMOND)
                .addOptional(BCCoreItems.gearDiamond.getId())
        ;
        tag(OreDictionaryTags.GEARS)
                .addOptionalTag(OreDictionaryTags.GEAR_WOOD.location())
                .addOptionalTag(OreDictionaryTags.GEAR_STONE.location())
                .addOptionalTag(OreDictionaryTags.GEAR_IRON.location())
                .addOptionalTag(OreDictionaryTags.GEAR_GOLD.location())
                .addOptionalTag(OreDictionaryTags.GEAR_DIAMOND.location())
        ;

        // tool
        tag(OreDictionaryTags.WRENCH)
                .addOptional(BCCoreItems.wrench.getId())
        ;

        addAllOptional(tag(OreDictionaryTags.PAINT_BRUSH), BCCoreItems.colourBrushMap.values().stream().map(reg -> reg));

        // sealant
        tag(OreDictionaryTags.SEALANT)
                .addOptionalTag(Tags.Items.DYES_GREEN.location())
                .addOptionalTag(Tags.Items.SLIMEBALLS.location())
        ;

        // misc
        tag(OreDictionaryTags.WORKBENCHES_ITEM)
                .addOptional(Items.CRAFTING_TABLE.getRegistryName())
        ;

        tag(OreDictionaryTags.CLAY)
                .addOptional(Items.CLAY.getRegistryName())
        ;

        // pipe plugs
        tag(OreDictionaryTags.WATERPROOF)
                .addOptional(BCTransportItems.waterproof.getId())
        ;

        // pipes
        OreDictionaryTags.pipeColorTags.forEach((c, t) ->
        {
            TagsProvider.TagAppender<Item> tagProvider = tag(t);
            PipeRegistry.INSTANCE.getAllRegisteredPipes().forEach(d -> tagProvider.addOptional(((Item) PipeRegistry.INSTANCE.getItemForPipe(d, c)).getRegistryName()));
        });

        addAllOptional(tag(OreDictionaryTags.pipeStructure), BCTransportItems.pipeStructure.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemWood), BCTransportItems.pipeItemWood.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeFluidWood), BCTransportItems.pipeFluidWood.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipePowerWood), BCTransportItems.pipePowerWood.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeRfWood), BCTransportItems.pipeRfWood.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemStone), BCTransportItems.pipeItemStone.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeFluidStone), BCTransportItems.pipeFluidStone.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipePowerStone), BCTransportItems.pipePowerStone.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeRfStone), BCTransportItems.pipeRfStone.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemCobble), BCTransportItems.pipeItemCobble.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeFluidCobble), BCTransportItems.pipeFluidCobble.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipePowerCobble), BCTransportItems.pipePowerCobble.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeRfCobble), BCTransportItems.pipeRfCobble.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemQuartz), BCTransportItems.pipeItemQuartz.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeFluidQuartz), BCTransportItems.pipeFluidQuartz.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipePowerQuartz), BCTransportItems.pipePowerQuartz.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeRfQuartz), BCTransportItems.pipeRfQuartz.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemGold), BCTransportItems.pipeItemGold.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeFluidGold), BCTransportItems.pipeFluidGold.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipePowerGold), BCTransportItems.pipePowerGold.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeRfGold), BCTransportItems.pipeRfGold.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemSandstone), BCTransportItems.pipeItemSandstone.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeFluidSandstone), BCTransportItems.pipeFluidSandstone.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipePowerSandstone), BCTransportItems.pipePowerSandstone.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeRfSandstone), BCTransportItems.pipeRfSandstone.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemIron), BCTransportItems.pipeItemIron.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeFluidIron), BCTransportItems.pipeFluidIron.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipePowerIron), BCTransportItems.pipePowerIron.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeRfIron), BCTransportItems.pipeRfIron.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemDiamond), BCTransportItems.pipeItemDiamond.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeFluidDiamond), BCTransportItems.pipeFluidDiamond.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipePowerDiamond), BCTransportItems.pipePowerDiamond.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeRfDiamond), BCTransportItems.pipeRfDiamond.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemDiaWood), BCTransportItems.pipeItemDiaWood.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeFluidDiaWood), BCTransportItems.pipeFluidDiaWood.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipePowerDiaWood), BCTransportItems.pipePowerDiaWood.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeRfDiaWood), BCTransportItems.pipeRfDiaWood.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemClay), BCTransportItems.pipeItemClay.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeFluidClay), BCTransportItems.pipeFluidClay.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemVoid), BCTransportItems.pipeItemVoid.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeFluidVoid), BCTransportItems.pipeFluidVoid.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemObsidian), BCTransportItems.pipeItemObsidian.values().stream().map(reg -> reg));
//        addAllOptional(tag(OreDictionaryTags.pipeFluidObsidian), BCTransportItems.pipeFluidObsidian.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemLapis), BCTransportItems.pipeItemLapis.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemDaizuli), BCTransportItems.pipeItemDaizuli.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemEmzuli), BCTransportItems.pipeItemEmzuli.values().stream().map(reg -> reg));
        addAllOptional(tag(OreDictionaryTags.pipeItemStripes), BCTransportItems.pipeItemStripes.values().stream().map(reg -> reg));
    }

    private static void addAllOptional(TagAppender<Item> tag, Stream<RegistryObject<?>> allToAdd) {
        allToAdd.forEach(reg -> tag.addOptional(reg.getId()));
    }

    @Override
    public String getName() {
        return "BuildCraft Item Tags Generator";
    }
}
