package buildcraft.api.fuels;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public final class IronEngineCoolant {

	public static BiMap<String, Coolant> liquidCoolants = HashBiMap.create();
	public static Map<ItemData, FluidStack> solidCoolants = new HashMap<ItemData, FluidStack>();

	public static FluidStack getFluidCoolant(ItemStack stack) {
		return solidCoolants.get(new ItemData(stack.itemID, stack.getItemDamage()));
	}

	public static Coolant getCoolant(ItemStack stack) {
		return getCoolant(getFluidCoolant(stack));
	}

	public static Coolant getCoolant(FluidStack liquid) {
	    return liquid != null ? liquidCoolants.get(liquid.getFluid().getName()) : null;
	}

	private IronEngineCoolant() {
	}

	public static interface Coolant {

		float getDegreesCoolingPerMB(float currentHeat);
	}

	public static void addCoolant(final Fluid liquid, final float degreesCoolingPerMB) {
		if (liquid != null) {
			liquidCoolants.put(liquid.getName(), new Coolant() {
				@Override
				public float getDegreesCoolingPerMB(float currentHeat) {
					return degreesCoolingPerMB;
				}
			});
		}
	}

	public static void addCoolant(final int itemId, final int metadata, final FluidStack coolant) {
		if (Item.itemsList[itemId] != null && coolant != null) {
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

    public static boolean isCoolant(Fluid fluid)
    {
        return liquidCoolants.inverse().containsKey(fluid);
    }
}
