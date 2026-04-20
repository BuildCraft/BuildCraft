package ct.buildcraft.core;

import java.util.function.Consumer;

import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.common.Tags;

public class BCCoreRecipesProvider extends RecipeProvider{
	
	public BCCoreRecipesProvider(DataGenerator p_125973_) {
		super(p_125973_);
	}

	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> writer) {
        if (BCCoreItems.PAINT_BRUSH.isPresent()) {
            ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(BCCoreItems.PAINT_BRUSH.get());
            builder.pattern(" iw");
            builder.pattern(" gi");
            builder.pattern("s  ");
            builder.define('i', Tags.Items.STRING);
            builder.define('s', Tags.Items.RODS_WOODEN);
            builder.define('g', BCCoreItems.GEAR_WOOD.get());//TODO use Tags
            builder.define('w', ItemTags.WOOL);
            builder.group("buildcraftcore:paintbrush");
            builder.unlockedBy("has_"+BCCoreItems.GEAR_WOOD.get().getDescriptionId()
            		, TriggerInstance.hasItems(BCCoreItems.GEAR_WOOD.get()));
            builder.save(writer);

            for (DyeColor colour : DyeColor.values()) {
/*                ItemPaintbrush_BC8.Brush brush = BCCoreItems.paintbrush.new Brush(colour);
                ItemStack out = brush.save();

                Object[] inputs = { //
                    cleanPaintbrush, //
                    ColourUtil.getDyeName(colour),//
                };
                ResourceLocation group = BCModules.CORE.createLocation("paintbrush_colouring");
                ShapelessOreRecipe recipe = new ShapelessOreRecipe(group, out, inputs);
                recipe.setRegistryName(BCModules.CORE.createLocation("paintbrush_" + colour.getName()));
                event.getRegistry().register(recipe)
            }*/
        }

        // if (BCItems.CORE_LIST != null) {
        // if (BCBlocks.SILICON_TABLE_ASSEMBLY != null) {
        // long mjCost = 2_000 * MjAPI.MJ;
        // ImmutableSet<StackDefinition> required = ImmutableSet.of(//
        // ArrayStackFilter.definition(8, Items.PAPER), //
        // OreStackFilter.definition(ColourUtil.getDyeName(DyeColor.GREEN)), //
        // OreStackFilter.definition("dustRedstone")//
        // );
        // BuildcraftRecipeRegistry.assemblyRecipes
        // .patternRecipe(new AssemblyRecipe("list", mjCost, required, new ItemStack(BCItems.CORE_LIST)));
        // } else {
        // // handled in JSON
        // }
        // }

/*        if (BCBlocks.Core.DECORATED != null) {
            ShapedRecipeBuilder builder = new ShapedRecipeBuilder();
            builder.pattern("sss");
            builder.pattern("scs");
            builder.pattern("sss");

            // if (BCItems.Builders!= null) {
            // builder.define('s', "stone");
            // builder.define('c', new ItemStack(BCItems.Builders.SNAPSHOT, 1, 2));
            // builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16, EnumDecoratedBlock.BLUEPRINT.ordinal()));
            // builder.register();
            //
            // builder.define('c', new ItemStack(BCItems.Builders.SNAPSHOT, 1, 0));
            // builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16, EnumDecoratedBlock.TEMPLATE.ordinal()));
            // builder.register();
            // }

            builder.define('s', Blocks.OBSIDIAN);
            builder.define('c', Blocks.REDSTONE_BLOCK);
            builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16, EnumDecoratedBlock.LASER_BACK.ordinal()));
            builder.register();*/
        }
	}
	
	
}
