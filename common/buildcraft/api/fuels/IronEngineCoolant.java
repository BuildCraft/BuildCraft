package buildcraft.api.fuels;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidDictionary;

import net.minecraftforge.liquids.LiquidStack;

public final class IronEngineCoolant {

	public static Map<String, Coolant> liquidCoolants = new HashMap<String, Coolant>();
	public static Map<ItemData, LiquidStack> solidCoolants = new HashMap<ItemData, LiquidStack>();

	public static LiquidStack getLiquidCoolant(ItemStack stack) {
		return solidCoolants.get(new ItemData(stack.itemID, stack.getItemDamage()));
	}

	public static Coolant getCoolant(ItemStack stack) {
		return getCoolant(getLiquidCoolant(stack));
	}

	public static Coolant getCoolant(LiquidStack liquid) {
		if (liquid == null)
			return null;
		if (liquid.itemID <= 0)
			return null;

		String fluidId = LiquidDictionary.findLiquidName(liquid);
		if (fluidId != null) {
			return liquidCoolants.get(fluidId);
		}

		return null;
	}

	private IronEngineCoolant() {
	}

	public static interface Coolant {

		float getDegreesCoolingPerMB(float currentHeat);
	}

	public static void addCoolant(final LiquidStack liquid, final float degreesCoolingPerMB) {
		String fluidId = LiquidDictionary.findLiquidName(liquid);
		if (fluidId != null) {
			liquidCoolants.put(fluidId, new Coolant() {
				@Override
				public float getDegreesCoolingPerMB(float currentHeat) {
					return degreesCoolingPerMB;
				}
			});
		}
	}

	public static void addCoolant(final int itemId, final int metadata, final LiquidStack coolant) {
		if (Item.itemsList[itemId] != null && coolant != null && coolant.amount > 0) {
			solidCoolants.put(new ItemData(itemId, metadata), coolant);
		}
	}

	public static class ItemData {

		public final int itemId, meta;

		public ItemData(int itemId, int meta) {
			this.itemId = itemId;
			this.meta = meta;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 67 * hash + this.itemId;
			hash = 67 * hash + this.meta;
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ItemData other = (ItemData) obj;
			if (this.itemId != other.itemId)
				return false;
			if (this.meta != other.meta)
				return false;
			return true;
		}
	}
}
