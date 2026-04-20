package ct.buildcraft.lib.recipe;

import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class NbtShapedRecipeBuilder extends ShapedRecipeBuilder{
	
	protected CompoundTag nbt;
	
	public NbtShapedRecipeBuilder(ItemStack stack) {
		super(stack.getItem(), stack.getCount());
		nbt = stack.getOrCreateTag();
	}

	public NbtShapedRecipeBuilder(ItemLike item) {
		super(item, 1);
		nbt = new CompoundTag();
	}
	
	public NbtShapedRecipeBuilder(ItemLike p_126114_, int p_126115_) {
		super(p_126114_, p_126115_);
		nbt = new CompoundTag();
	}
	
	public CompoundTag getTag() {
		return nbt;
	}
	
	@Override
	public void save(Consumer<FinishedRecipe> com, ResourceLocation p_126142_) {
//		if(nbt.isEmpty())
//			throw new IllegalStateException("No NBT data!You should use ShapedRecipeBuilder instead. "+p_126142_);
		super.save((a) ->{
			com.accept(new ResultHolder(nbt, a));
		}, p_126142_);
	}
	
	public static class ResultHolder implements FinishedRecipe{
		private final CompoundTag tag;
		private final FinishedRecipe result;
		
		public ResultHolder(CompoundTag tag, FinishedRecipe result) {
			this.tag = tag;
			this.result = result;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			result.serializeRecipeData(json);
			JsonObject json1 = json.getAsJsonObject("result");
			json1.addProperty("nbt", tag.getAsString());
			json.add("result", json1);
		}
		
		public CompoundTag geTag() {
			return tag;
		}

		@Override
		public ResourceLocation getId() {
			return result.getId();
		}

		@Override
		public RecipeSerializer<?> getType() {
			return result.getType();
		}

		@Override
		public JsonObject serializeAdvancement() {
			return result.serializeAdvancement();
		}

		@Override
		public ResourceLocation getAdvancementId() {
			return result.getAdvancementId();
		}
		
		
		
	}
}
