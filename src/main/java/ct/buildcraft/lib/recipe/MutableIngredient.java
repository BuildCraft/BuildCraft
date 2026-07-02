package ct.buildcraft.lib.recipe;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public class MutableIngredient extends Ingredient{

	private Ingredient deleIngredient = Ingredient.EMPTY;
	
	public MutableIngredient() {
		super(Stream.empty());
	}
	
	public void setDelegate(@Nonnull ItemLike item) {
		Objects.nonNull(item);
		deleIngredient = Ingredient.of(item);
	}
	
	public void setDelegate(@Nonnull TagKey<Item> tag) {
		Objects.nonNull(tag);
		deleIngredient = Ingredient.of(tag);
	}
	
	public void setDelegate(@Nonnull Ingredient deIngredient) {
		Objects.nonNull(deIngredient);
		deleIngredient = deIngredient;
	}

	@Override
	public Predicate<ItemStack> and(Predicate<? super ItemStack> other) {
		return deleIngredient.and(other);
	}

	@Override
	public Predicate<ItemStack> negate() {
		return deleIngredient.negate();
	}

	@Override
	public Predicate<ItemStack> or(Predicate<? super ItemStack> other) {
		return deleIngredient.or(other);
	}

	@Override
	public ItemStack[] getItems() {
		return deleIngredient.getItems();
	}

	@Override
	public boolean test(ItemStack p_43914_) {
		return deleIngredient.test(p_43914_);
	}

	@Override
	public IntList getStackingIds() {
		return deleIngredient.getStackingIds();
	}

	@Override
	public JsonElement toJson() {
		return deleIngredient.toJson();
	}

	@Override
	public boolean isEmpty() {
		return deleIngredient.isEmpty();
	}

	@Override
	protected void invalidate() {
		//No-OP
	}

	@Override
	public boolean isSimple() {
		return deleIngredient.isSimple();
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return deleIngredient.getSerializer();
	}
	
	

}
