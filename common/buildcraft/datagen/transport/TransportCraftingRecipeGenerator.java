package buildcraft.datagen.transport;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.transport.BCTransport;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportConfig;
import buildcraft.transport.BCTransportItems;
import net.minecraft.data.*;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.RegistryObject;

import java.util.Map;
import java.util.function.Consumer;

public class TransportCraftingRecipeGenerator extends RecipeProvider {
    private static final String MOD_ID = BCTransport.MODID;

    public TransportCraftingRecipeGenerator(DataGenerator generator) {
        super(generator);
    }

    private Consumer<IFinishedRecipe> consumer;

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        this.consumer = consumer;
        // filtered_buffer
        ShapedRecipeBuilder.shaped(BCTransportBlocks.filteredBuffer.get())
                .pattern("wdw")
                .pattern("wcw")
                .pattern("wpw")
                .define('p', Items.PISTON)
                .define('c', Tags.Items.CHESTS_WOODEN)
                .define('d', OreDictionaryTags.pipeItemDiamond)
                .define('w', ItemTags.PLANKS)
                .unlockedBy("has_item", has(BCBuildersBlocks.quarry.get()))
                .group(MOD_ID)
                .save(consumer);
        // waterproof
        ShapelessRecipeBuilder.shapeless(BCTransportItems.waterproof.get())
                .requires(OreDictionaryTags.SEALANT)
                .unlockedBy("has_item", has(OreDictionaryTags.SEALANT))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":pipe_sealant");
        // pipe_structure
        ShapedRecipeBuilder.shaped((Item) BCTransportItems.pipeStructure.get(null).get(), 8)
                .pattern("cgc")
                .define('c', Tags.Items.COBBLESTONE)
                .define('g', Tags.Items.GRAVEL)
                .unlockedBy("has_item", has(Tags.Items.COBBLESTONE))
                .group(MOD_ID)
                .save(consumer);
        // plug_blocker
        ShapelessRecipeBuilder.shapeless(BCTransportItems.plugBlocker.get(), 4)
                .requires(OreDictionaryTags.pipeStructure)
                .unlockedBy("has_item", has(Tags.Items.COBBLESTONE))
                .group(MOD_ID)
                .save(consumer);
        // plug_power_adaptor
        ShapedRecipeBuilder.shaped(BCTransportItems.plugPowerAdaptor.get(), 4)
                .pattern("sis")
                .pattern("sgs")
                .pattern("srs")
                .define('s', OreDictionaryTags.pipeStructure)
                .define('g', OreDictionaryTags.GEAR_STONE)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_GOLD)
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_STONE))
                .group(MOD_ID)
                .save(consumer);

        // pipes
        addPipeRecipe(BCTransportItems.pipeItemWood, ItemTags.PLANKS);
        addPipeRecipe(BCTransportItems.pipeItemCobble, Tags.Items.COBBLESTONE);
        addPipeRecipe(BCTransportItems.pipeItemStone, Tags.Items.STONE);
        addPipeRecipe(BCTransportItems.pipeItemQuartz, Tags.Items.STORAGE_BLOCKS_QUARTZ);
        addPipeRecipe(BCTransportItems.pipeItemIron, Tags.Items.INGOTS_IRON);
        addPipeRecipe(BCTransportItems.pipeItemGold, Tags.Items.INGOTS_GOLD);
        addPipeRecipe(BCTransportItems.pipeItemClay, OreDictionaryTags.CLAY);
        addPipeRecipe(BCTransportItems.pipeItemSandstone, Tags.Items.SANDSTONE);
        addPipeRecipe(BCTransportItems.pipeItemVoid, Tags.Items.DYES_BLACK, Tags.Items.DUSTS_REDSTONE);
        addPipeRecipe(BCTransportItems.pipeItemObsidian, Tags.Items.OBSIDIAN);
        addPipeRecipe(BCTransportItems.pipeItemDiamond, Tags.Items.GEMS_DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemLapis, Tags.Items.STORAGE_BLOCKS_LAPIS);
        addPipeRecipe(BCTransportItems.pipeItemDaizuli, Tags.Items.STORAGE_BLOCKS_LAPIS, Tags.Items.GEMS_DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemDiaWood, ItemTags.PLANKS, Tags.Items.GEMS_DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemStripes, OreDictionaryTags.GEAR_GOLD);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipeItemEmzuli, Tags.Items.STORAGE_BLOCKS_LAPIS);

        INamedTag<Item> waterproof = OreDictionaryTags.WATERPROOF;
        addPipeUpgradeRecipe(BCTransportItems.pipeItemWood, BCTransportItems.pipeFluidWood, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemCobble, BCTransportItems.pipeFluidCobble, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemStone, BCTransportItems.pipeFluidStone, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemQuartz, BCTransportItems.pipeFluidQuartz, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipeFluidIron, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemGold, BCTransportItems.pipeFluidGold, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemClay, BCTransportItems.pipeFluidClay, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemSandstone, BCTransportItems.pipeFluidSandstone, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemVoid, BCTransportItems.pipeFluidVoid, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemObsidian, BCTransportItems.pipeFluidObsidian, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipeFluidDiamond, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipeFluidDiaWood, waterproof);

        INamedTag<Item> upgrade = Tags.Items.DUSTS_REDSTONE;
        addPipeUpgradeRecipe(BCTransportItems.pipeItemWood, BCTransportItems.pipePowerWood, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemCobble, BCTransportItems.pipePowerCobble, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemStone, BCTransportItems.pipePowerStone, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemQuartz, BCTransportItems.pipePowerQuartz, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipePowerIron, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemGold, BCTransportItems.pipePowerGold, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemSandstone, BCTransportItems.pipePowerSandstone, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipePowerDiamond, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipePowerDiaWood, upgrade);

        if (!BCTransportConfig.disableRfPipe) {
            addPipeUpgradeRecipe(BCTransportItems.pipePowerWood, BCTransportItems.pipeRfWood, upgrade);
            addPipeUpgradeRecipe(BCTransportItems.pipePowerCobble, BCTransportItems.pipeRfCobble, upgrade);
            addPipeUpgradeRecipe(BCTransportItems.pipePowerStone, BCTransportItems.pipeRfStone, upgrade);
            addPipeUpgradeRecipe(BCTransportItems.pipePowerQuartz, BCTransportItems.pipeRfQuartz, upgrade);
            addPipeUpgradeRecipe(BCTransportItems.pipePowerIron, BCTransportItems.pipeRfIron, upgrade);
            addPipeUpgradeRecipe(BCTransportItems.pipePowerGold, BCTransportItems.pipeRfGold, upgrade);
            addPipeUpgradeRecipe(BCTransportItems.pipePowerSandstone, BCTransportItems.pipeRfSandstone, upgrade);
            addPipeUpgradeRecipe(BCTransportItems.pipePowerDiamond, BCTransportItems.pipeRfDiamond, upgrade);
            addPipeUpgradeRecipe(BCTransportItems.pipePowerDiaWood, BCTransportItems.pipeRfDiaWood, upgrade);
        }
    }

    private void addPipeRecipe(Map<DyeColor, RegistryObject<? extends IItemPipe>> pipe, INamedTag material) {
        addPipeRecipe(pipe, material, material);
    }

    private void addPipeRecipe(Map<DyeColor, RegistryObject<? extends IItemPipe>> pipe, INamedTag left, INamedTag right) {
        if (pipe == null) {
            return;
        }
        Item colourless = (Item) pipe.get(null).get();
        ShapedRecipeBuilder.shaped(colourless, 8)
                .pattern("lgr")
                .define('l', left)
                .define('g', Tags.Items.GLASS_COLORLESS)
                .define('r', right)
                .unlockedBy("has_item", has(Tags.Items.GLASS))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":" + colourless.getRegistryName().getPath() + "_colorless");
        for (DyeColor colour : DyeColor.values()) {
            Item coloured = (Item) pipe.get(colour).get();
            ShapedRecipeBuilder.shaped(coloured, 8)
                    .pattern("lgr")
                    .define('l', left)
                    .define('g', ItemTags.createOptional(new ResourceLocation("forge:glass/" + colour.getName())))
                    .define('r', right)
                    .unlockedBy("has_item", has(Tags.Items.GLASS))
                    .group(MOD_ID)
                    .save(consumer, MOD_ID + ":" + coloured.getRegistryName().getPath() + "_" + colour.getName());
        }
    }

    private void addPipeUpgradeRecipe(Map<DyeColor, RegistryObject<? extends IItemPipe>> from, Map<DyeColor, RegistryObject<? extends IItemPipe>> to, INamedTag<Item> additional) {
        if (from == null || to == null) {
            return;
        }
        if (additional == null) {
            throw new NullPointerException("additional");
        }
        Item from_colourless = (Item) from.get(null).get();
        Item to_colourless = (Item) to.get(null).get();
        ShapelessRecipeBuilder.shapeless(from_colourless)
                .requires(to_colourless)
                .unlockedBy("has_item", has(Tags.Items.GLASS))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":" + to_colourless.getRegistryName().getPath() + "_colorless_undo");
        ShapelessRecipeBuilder.shapeless(to_colourless)
                .requires(from_colourless)
                .requires(additional)
                .unlockedBy("has_item", has(Tags.Items.GLASS))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":" + to_colourless.getRegistryName().getPath() + "_colorless");

        for (DyeColor colour : ColourUtil.COLOURS) {
            Item from_coloured = (Item) from.get(colour).get();
            Item to_coloured = (Item) to.get(colour).get();
            ShapelessRecipeBuilder.shapeless(from_coloured)
                    .requires(to_coloured)
                    .unlockedBy("has_item", has(Tags.Items.GLASS))
                    .group(MOD_ID)
                    .save(consumer, MOD_ID + ":" + to_coloured.getRegistryName().getPath() + "_" + colour.getName() + "_undo");
            ShapelessRecipeBuilder.shapeless(to_coloured)
                    .requires(from_coloured)
                    .requires(additional)
                    .unlockedBy("has_item", has(Tags.Items.GLASS))
                    .group(MOD_ID)
                    .save(consumer, MOD_ID + ":" + to_coloured.getRegistryName().getPath() + "_" + colour.getName());
        }
    }

    @Override
    public String getName() {
        return "BuildCraft Transport Crafting IRecipe Generator";
    }
}
