package ct.buildcraft.factory;

import java.util.function.Consumer;

import ct.buildcraft.core.BCCoreItems;

import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class BCFactoryRecipesProvider extends RecipeProvider{

	public BCFactoryRecipesProvider(DataGenerator p_125973_) {
		super(p_125973_);
	}

	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> writer) {                
    	ShapedRecipeBuilder tank = ShapedRecipeBuilder.shaped(BCFactoryItems.TANK_BLOCK_ITEM.get(), 1);
        tank
        .pattern("ggg")
        .pattern("g g")
        .pattern("ggg")
        .define('g', Items.GLASS)
        .unlockedBy("has_"+Items.GLASS.getDescriptionId(), TriggerInstance.hasItems(Items.GLASS))
        .save(writer);
        
    	ShapedRecipeBuilder mining_well = ShapedRecipeBuilder.shaped(BCFactoryItems.MINING_WELL_BLOCK_ITEM.get(), 1);
        mining_well
        .pattern("iri")
        .pattern("igi")
        .pattern("iai")
        .define('i', Items.IRON_INGOT)
        .define('r', Items.REDSTONE)
        .define('g', BCCoreItems.GEAR_IRON.get())
        .define('a', Items.IRON_PICKAXE)
        .unlockedBy("has_"+BCCoreItems.GEAR_IRON.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.GEAR_IRON.get()))
        .save(writer);
        
    	ShapedRecipeBuilder pump = ShapedRecipeBuilder.shaped(BCFactoryItems.PUMP_BLOCK_ITEM.get(), 1);
        pump
        .pattern("iri")
        .pattern("igi")
        .pattern("tbt")
        .define('i', Items.IRON_INGOT)
        .define('r', Items.REDSTONE)
        .define('g', BCCoreItems.GEAR_IRON.get())
        .define('b', Items.BUCKET)
        .define('t', BCFactoryItems.TANK_BLOCK_ITEM.get())
        .unlockedBy("has_"+BCCoreItems.GEAR_IRON.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.GEAR_IRON.get()))
        .save(writer);
        
    	ShapedRecipeBuilder flood_gate = ShapedRecipeBuilder.shaped(BCFactoryItems.FLOOD_GATE_BLOCK_ITEM.get(), 1);
        flood_gate
        .pattern("igi")
        .pattern("ftf")
        .pattern("ifi")
        .define('i', Items.IRON_INGOT)
        .define('f', Items.IRON_BARS)
        .define('g', BCCoreItems.GEAR_IRON.get())
        .define('t', BCFactoryItems.TANK_BLOCK_ITEM.get())
        .unlockedBy("has_"+BCCoreItems.GEAR_IRON.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.GEAR_IRON.get()))
        .save(writer);
        
    	ShapedRecipeBuilder heat_exchange = ShapedRecipeBuilder.shaped(BCFactoryItems.HEAT_EXCHANGE_BLOCK_ITEM.get(), 1);
        heat_exchange
        .pattern("iei")
        .pattern("ggg")
        .pattern("iei")
        .define('g', Items.GLASS)
        .define('i', Items.IRON_INGOT)
        .define('e', BCCoreItems.GEAR_IRON.get())
        .unlockedBy("has_"+BCCoreItems.GEAR_IRON.get().getDescriptionId(), TriggerInstance.hasItems(BCCoreItems.GEAR_IRON.get()))
        .save(writer);
        
    	ShapedRecipeBuilder builder6 = ShapedRecipeBuilder.shaped(BCFactoryItems.DISTILLER_BLOCK_ITEM.get(), 1);
        builder6
        .pattern("rtr")
        .pattern("ter")
        .pattern("   ")
        .define('r', Items.REDSTONE_TORCH)
        .define('t', BCFactoryItems.TANK_BLOCK_ITEM.get())
        .define('e', BCCoreItems.GEAR_DIAMOND.get())
        .unlockedBy("has_"+BCCoreItems.GEAR_DIAMOND.get().getDescriptionId(), TriggerInstance.hasItems(BCCoreItems.GEAR_DIAMOND.get()))
        .save(writer);
        
    	ShapedRecipeBuilder builder7 = ShapedRecipeBuilder.shaped(BCFactoryItems.AUTO_BENCH_ITEM.get(), 1);
        builder7
        .pattern(" s ")
        .pattern(" c ")
        .pattern(" s ")
        .define('c', Items.CRAFTING_TABLE)
        .define('s', BCCoreItems.GEAR_STONE.get())
        .unlockedBy("has_"+BCCoreItems.GEAR_STONE.get().getDescriptionId(), TriggerInstance.hasItems(BCCoreItems.GEAR_STONE.get()))
        .save(writer, new ResourceLocation(BCFactory.MODID,"autowork_bench_1"));
        
    	ShapedRecipeBuilder builder8 = ShapedRecipeBuilder.shaped(BCFactoryItems.AUTO_BENCH_ITEM.get(), 1);
        builder8
        .pattern("   ")
        .pattern("scs")
        .pattern("   ")
        .define('c', Items.CRAFTING_TABLE)
        .define('s', BCCoreItems.GEAR_STONE.get())
        .unlockedBy("has_"+BCCoreItems.GEAR_STONE.get().getDescriptionId(), TriggerInstance.hasItems(BCCoreItems.GEAR_STONE.get()))
        .save(writer, new ResourceLocation(BCFactory.MODID,"autowork_bench_2"));
        
        
        super.buildCraftingRecipes(writer);
	}
	

}
