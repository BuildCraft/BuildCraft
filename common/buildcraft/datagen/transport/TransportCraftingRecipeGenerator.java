package buildcraft.datagen.transport;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.ItemUtil;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.transport.BCTransport;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;
import java.util.function.Consumer;

public class TransportCraftingRecipeGenerator extends RecipeProvider {
    private static final String MOD_ID = BCTransport.MODID;

    public TransportCraftingRecipeGenerator(PackOutput packOutput) {
        super(packOutput);
    }

    private Consumer<FinishedRecipe> consumer;

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        this.consumer = consumer;
        // filtered_buffer
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCTransportBlocks.filteredBuffer.get())
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
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BCTransportItems.waterproof.get())
                .requires(OreDictionaryTags.SEALANT)
                .unlockedBy("has_item", has(OreDictionaryTags.SEALANT))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":pipe_sealant");
        // pipe_structure
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, (Item) BCTransportItems.pipeStructure.get(null).get(), 8)
                .pattern("cgc")
                .define('c', Tags.Items.COBBLESTONE)
                .define('g', Tags.Items.GRAVEL)
                .unlockedBy("has_item", has(Tags.Items.COBBLESTONE))
                .group(MOD_ID)
                .save(consumer);
        // plug_blocker
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BCTransportItems.plugBlocker.get(), 4)
                .requires(OreDictionaryTags.pipeStructure)
                .unlockedBy("has_item", has(Tags.Items.COBBLESTONE))
                .group(MOD_ID)
                .save(consumer);
        // plug_power_adaptor
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCTransportItems.plugPowerAdaptor.get(), 4)
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

        TagKey<Item> waterproof = OreDictionaryTags.waterproof;
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

        TagKey<Item> upgrade = Tags.Items.DUSTS_REDSTONE;
        addPipeUpgradeRecipe(BCTransportItems.pipeItemWood, BCTransportItems.pipePowerWood, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemCobble, BCTransportItems.pipePowerCobble, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemStone, BCTransportItems.pipePowerStone, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemQuartz, BCTransportItems.pipePowerQuartz, upgrade);
        // addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipePowerIron, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemGold, BCTransportItems.pipePowerGold, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemSandstone, BCTransportItems.pipePowerSandstone, upgrade);
        // addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipePowerDiamond, upgrade);
    }

    private void addPipeRecipe(Map<DyeColor, RegistryObject<? extends IItemPipe>> pipe, TagKey material) {
        addPipeRecipe(pipe, material, material);
    }

    private void addPipeRecipe(Map<DyeColor, RegistryObject<? extends IItemPipe>> pipe, TagKey left, TagKey right) {
        if (pipe == null) {
            return;
        }
        Item colourless = (Item) pipe.get(null).get();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, colourless, 8)
                .pattern("lgr")
                .define('l', left)
                .define('g', Tags.Items.GLASS_COLORLESS)
                .define('r', right)
                .unlockedBy("has_item", has(Tags.Items.GLASS))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":" + ItemUtil.getRegistryName(colourless).getPath() + "_colorless");
        for (DyeColor colour : DyeColor.values()) {
            Item coloured = (Item) pipe.get(colour).get();
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, coloured, 8)
                    .pattern("lgr")
                    .define('l', left)
                    .define('g', TagKey.create(Registries.ITEM, new ResourceLocation("forge:glass/" + colour.getName())))
                    .define('r', right)
                    .unlockedBy("has_item", has(Tags.Items.GLASS))
                    .group(MOD_ID)
                    .save(consumer, MOD_ID + ":" + ItemUtil.getRegistryName(coloured).getPath() + "_" + colour.getName());
        }
    }

    private void addPipeUpgradeRecipe(Map<DyeColor, RegistryObject<? extends IItemPipe>> from, Map<DyeColor, RegistryObject<? extends IItemPipe>> to, TagKey<Item> additional) {
        if (from == null || to == null) {
            return;
        }
        if (additional == null) {
            throw new NullPointerException("additional");
        }
        Item from_colourless = (Item) from.get(null).get();
        Item to_colourless = (Item) to.get(null).get();
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, from_colourless)
                .requires(to_colourless)
                .unlockedBy("has_item", has(Tags.Items.GLASS))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":" + ItemUtil.getRegistryName(to_colourless).getPath() + "_colorless_undo");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, to_colourless)
                .requires(from_colourless)
                .requires(additional)
                .unlockedBy("has_item", has(Tags.Items.GLASS))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":" + ItemUtil.getRegistryName(to_colourless).getPath() + "_colorless");

        for (DyeColor colour : ColourUtil.COLOURS) {
            Item from_coloured = (Item) from.get(colour).get();
            Item to_coloured = (Item) to.get(colour).get();
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, from_coloured)
                    .requires(to_coloured)
                    .unlockedBy("has_item", has(Tags.Items.GLASS))
                    .group(MOD_ID)
                    .save(consumer, MOD_ID + ":" + ItemUtil.getRegistryName(to_coloured).getPath() + "_" + colour.getName() + "_undo");
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, to_coloured)
                    .requires(from_coloured)
                    .requires(additional)
                    .unlockedBy("has_item", has(Tags.Items.GLASS))
                    .group(MOD_ID)
                    .save(consumer, MOD_ID + ":" + ItemUtil.getRegistryName(to_coloured).getPath() + "_" + colour.getName());
        }
    }

    @Override
    public String getName() {
        return "BuildCraft Transport Crafting Recipe Generator";
    }
}
