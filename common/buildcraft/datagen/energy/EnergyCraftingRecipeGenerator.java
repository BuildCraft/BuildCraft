package buildcraft.datagen.energy;

import buildcraft.energy.BCEnergy;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.transport.BCTransportItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class EnergyCraftingRecipeGenerator extends RecipeProvider {
    private static final String MOD_ID = BCEnergy.MODID;

    public EnergyCraftingRecipeGenerator(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // engineIron
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCEnergyBlocks.engineIron.get())
                .pattern("www")
                .pattern(" g ")
                .pattern("GpG")
                .define('w', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('g', Ingredient.of(Tags.Items.GLASS_COLORLESS))
                .define('G', Ingredient.of(OreDictionaryTags.GEAR_IRON))
                .define('p', Ingredient.of(Items.PISTON))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_IRON))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":engine_iron");
        // engineStone
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCEnergyBlocks.engineStone.get())
                .pattern("www")
                .pattern(" g ")
                .pattern("GpG")
                .define('w', Ingredient.of(Tags.Items.COBBLESTONE))
                .define('g', Ingredient.of(Tags.Items.GLASS_COLORLESS))
                .define('G', Ingredient.of(OreDictionaryTags.GEAR_STONE))
                .define('p', Ingredient.of(Items.PISTON))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_STONE))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":engine_stone");
        // waterproof
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BCTransportItems.waterproof.get(), 8)
                .requires(BCEnergyFluids.oilResidue[0].get().getBucket())
                .unlockedBy("has_item", has(BCFactoryBlocks.distiller.get()))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":residue_to_pipe_sealant");
    }

    @Override
    public String getName() {
        return "BuildCraft Energy Crafting Recipe Generator";
    }
}
