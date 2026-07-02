package ct.buildcraft.lib.recipe;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import ct.buildcraft.api.core.BuildCraftAPI;
import ct.buildcraft.api.recipes.IngredientStack;
import ct.buildcraft.silicon.BCSiliconRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class AssemblyRecipe extends AssemblyRecipeBasic{

    final long requiredMicroJoules;
    final ImmutableSet<IngredientStack> requiredStacks;
    final ItemStack output;
    final String group;

    public AssemblyRecipe(ResourceLocation name, long requiredMicroJoules, ImmutableSet<IngredientStack> requiredStacks, @Nonnull ItemStack output, String group) {
        this.requiredMicroJoules = requiredMicroJoules;
        this.requiredStacks = ImmutableSet.copyOf(requiredStacks);
        this.output = output.copy();
        this.name = name;
		this.group = group == null ? "" : group;
    }

    public AssemblyRecipe(String name, long requiredMicroJoules, ImmutableSet<IngredientStack> requiredStacks, @Nonnull ItemStack output, String group) {
        this(BuildCraftAPI.nameToResourceLocation(name), requiredMicroJoules, requiredStacks, output, group);
    }

    public AssemblyRecipe(String name, long requiredMicroJoules, Set<IngredientStack> requiredStacks, @Nonnull ItemStack output, String group) {
        this(name, requiredMicroJoules, ImmutableSet.copyOf(requiredStacks), output, group);
    }
    
    public long getRequiredMicroJoulesFor(ItemStack output2) {
        return requiredMicroJoules;
    }
    
    @Override
	public Set<ItemStack> getOutputs(IItemHandlerModifiable inputs) {
		return ImmutableSet.of(assemble(new RecipeWrapper(inputs)));
	}
	
	@Override
	public boolean matches(Container container, Level level) {
		int size = container.getContainerSize();
		for(IngredientStack stack : requiredStacks) {
			boolean match = false;
			for(int i=0;i<size;i++) {
				ItemStack itemStack = container.getItem(i);
				if(!itemStack.isEmpty() && stack.ingredient.test(itemStack) && itemStack.getCount()>=stack.count) {
					match = true;
					break;
				}
			}
			if(!match)
				return false;
		}
		return true;
	}

	@Override
	public ItemStack assemble(Container container) {
		int size = container.getContainerSize();
		for(IngredientStack stack : requiredStacks) {
			boolean match = false;
			for(int i=0;i<size;i++) {
				ItemStack itemStack = container.getItem(i);
				if(!itemStack.isEmpty() && stack.ingredient.test(itemStack) && itemStack.getCount()>=stack.count) {
					match = true;
					break;
				}
			}
			if(!match)
				return ItemStack.EMPTY;
		}
		return output.copy();
	}

	@Override
	public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
		return true;//TODO
	}

	@Override
	public ItemStack getResultItem() {
		return output.copy();
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> list = NonNullList.create();
		for(IngredientStack stack : requiredStacks) {
			for(int i =0;i<stack.count;i++)
				list.add(stack.ingredient);
		}
		return list;
	}
	
	public Set<IngredientStack> getInputsFor(ItemStack output2) {
		return requiredStacks;
	}

	public Set<ItemStack> getOutputPreviews() {
		return ImmutableSet.of();
	}	

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return BCSiliconRecipes.ASSEMBLY_SERIALIZER.get();
	}
	
	public static class Serializer<C extends Container> implements RecipeSerializer<AssemblyRecipe>{
		@Override
		public AssemblyRecipe fromJson(ResourceLocation id, JsonObject json) {
			String group = GsonHelper.getAsString(json, "group", "");
			JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
			JsonArray counts = GsonHelper.getAsJsonArray(json, "ingredient_counts");
			Set<IngredientStack> stacks = new HashSet<>();
			if(ingredients.size() != counts.size())
				throw new JsonParseException("ingredients and counts don't match! recipe:"+id);
			for(int i = 0; i < ingredients.size(); ++i) {
				Ingredient ingredient = Ingredient.fromJson(ingredients.get(i));
				int count = counts.get(i).getAsInt();
				stacks.add(new IngredientStack(ingredient, count));
			}
			ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true, true);
			long power = GsonHelper.getAsLong(json, "MJ");
			return new AssemblyRecipe(id, power, ImmutableSet.copyOf(stacks), result, group);
		}

		@Override
		public @Nullable AssemblyRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
			String group = buf.readUtf();
			int size = buf.readInt();
			Set<IngredientStack> stacks = new HashSet<IngredientStack>();
			for(int i = 0; i < size; i++) {
				Ingredient ingredient = Ingredient.fromNetwork(buf);
				int count = buf.readInt();
				stacks.add(new IngredientStack(ingredient, count));
			}
			ItemStack result = buf.readItem();
			long power = buf.readLong();
			return new AssemblyRecipe(id, power, ImmutableSet.copyOf(stacks), result, group);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, AssemblyRecipe recipe) {
			buf.writeUtf(recipe.group);
			buf.writeInt(recipe.requiredStacks.size());
			for(IngredientStack stack : recipe.requiredStacks) {
				stack.ingredient.toNetwork(buf);
				buf.writeInt(stack.count);
			}
			buf.writeItem(recipe.output);
			buf.writeLong(recipe.requiredMicroJoules);
		}
		
	}
}
