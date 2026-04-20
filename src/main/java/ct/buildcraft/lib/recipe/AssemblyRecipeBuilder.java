package ct.buildcraft.lib.recipe;

import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.recipes.IngredientStack;
import ct.buildcraft.silicon.BCSiliconRecipes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public class AssemblyRecipeBuilder implements RecipeBuilder {
	protected final ItemStack result;
	protected final ImmutableSet<IngredientStack> ingredients;
	protected final Advancement.Builder advancement = Advancement.Builder.advancement();
	protected final long requiredMj;
	protected String group;

	public AssemblyRecipeBuilder(long requiredMj, ImmutableSet<IngredientStack> inputs, ItemStack output) {
		this.result = output;
		this.requiredMj = requiredMj;
		this.ingredients = inputs;
	}

	@Override
	public AssemblyRecipeBuilder unlockedBy(String s, CriterionTriggerInstance criterionTriggerInstance) {
		this.advancement.addCriterion(s, criterionTriggerInstance);
		return this;
	}

	@Override
	public AssemblyRecipeBuilder group(String group) {
		this.group = group;
		return this;
	}

	@Override
	public Item getResult() {
		return result.getItem();
	}

	@Override
	public void save(Consumer<FinishedRecipe> p_176503_, ResourceLocation id) {
	      this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);
	      p_176503_.accept(new AssemblyRecipeBuilder.Result(id, this.result, this.group == null ? "" : this.group, this.ingredients, this.requiredMj, this.advancement, new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath())));

	}

	public static class Result implements FinishedRecipe {
		private final ResourceLocation id;
		private final ItemStack result;
		private final String group;
		private final Set<IngredientStack> ingredients;
		private final Advancement.Builder advancement;
		private final ResourceLocation advancementId;
		private final long requireMj;

		public Result(ResourceLocation p_126222_, ItemStack p_126223_, String p_126225_,
				Set<IngredientStack> p_126226_, long requiredMj, Advancement.Builder p_126227_, ResourceLocation p_126228_) {
			this.id = p_126222_;
			this.result = p_126223_;
			this.group = p_126225_;
			this.ingredients = p_126226_;
			this.requireMj = requiredMj;
			this.advancement = p_126227_;
			this.advancementId = p_126228_;
		}

		public void serializeRecipeData(JsonObject json) {
			if (!this.group.isEmpty()) {
				json.addProperty("group", this.group);
			}

			JsonArray ingredients = new JsonArray();
			JsonArray ingredientCounts = new JsonArray();

			for (IngredientStack ingredientStack : this.ingredients) {
				JsonElement json2 = ingredientStack.ingredient.toJson();
				if(json2.isJsonObject()) {
					JsonObject jo = json2.getAsJsonObject();
					if(jo.has("item")&&jo.get("item").getAsString() == "minecraft:air")
						BCLog.logger.debug("error");
				}
				else if(json2.isJsonArray()){
					JsonArray ja = json2.getAsJsonArray();
					if(ja.isEmpty())
						BCLog.logger.debug("error");
				}
					
				ingredients.add(json2);
				ingredientCounts.add(ingredientStack.count);
			}

			json.add("ingredients", ingredients);
			json.add("ingredient_counts", ingredientCounts);
			JsonObject jsonobject = new JsonObject();
			jsonobject.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result.getItem()).toString());
			if (result.getCount() > 1) {
				jsonobject.addProperty("count", result.getCount());
			}
			if(result.hasTag())
				jsonobject.addProperty("nbt", result.getTag().toString());

			json.add("result", jsonobject);
			json.addProperty("MJ", requireMj);
		}

		public RecipeSerializer<?> getType() {
			return BCSiliconRecipes.ASSEMBLY_SERIALIZER.get();
		}

		public ResourceLocation getId() {
			return this.id;
		}

		@Nullable
		public JsonObject serializeAdvancement() {
			return this.advancement.serializeToJson();
		}

		@Nullable
		public ResourceLocation getAdvancementId() {
			return this.advancementId;
		}
	}

}
