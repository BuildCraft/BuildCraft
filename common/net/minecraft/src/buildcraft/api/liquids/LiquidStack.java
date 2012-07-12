package net.minecraft.src.buildcraft.api.liquids;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

/**
 * 
 * Forestry internal ItemStack substitute for liquids
 * 
 * @author SirSengir
 */
public class LiquidStack {
	public int itemID;
	public int amount;
	public int itemMeta;

	public NBTTagCompound stackTagCompound;

	private LiquidStack() {
	}

	public LiquidStack(int itemID, int liquidAmount) {
		this(itemID, liquidAmount, 0);
	}

	public LiquidStack(Item item, int liquidAmount) {
		this(item.shiftedIndex, liquidAmount, 0);
	}

	public LiquidStack(Block block, int liquidAmount) {
		this(block.blockID, liquidAmount, 0);
	}

	public LiquidStack(int itemID, int amount, int itemDamage) {
		this.itemID = itemID;
		this.amount = amount;
		this.itemMeta = itemDamage;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setShort("Id", (short) itemID);
		nbttagcompound.setInteger("Amount", amount);
		nbttagcompound.setShort("Meta", (short) itemMeta);
		if (stackTagCompound != null)
			nbttagcompound.setTag("Tag", stackTagCompound);
		return nbttagcompound;
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		itemID = nbttagcompound.getShort("Id");
		amount = nbttagcompound.getInteger("Amount");
		itemMeta = nbttagcompound.getShort("Meta");
		if (nbttagcompound.hasKey("Tag"))
			stackTagCompound = nbttagcompound.getCompoundTag("tag");
	}

	public LiquidStack copy() {
		LiquidStack copy = new LiquidStack(itemID, amount, itemMeta);
		if (stackTagCompound != null) {
			copy.stackTagCompound = (NBTTagCompound) stackTagCompound.copy();
			if (!copy.stackTagCompound.equals(stackTagCompound))
				return copy;
		}
		return copy;
	}

	public NBTTagCompound getTagCompound() {
		return stackTagCompound;
	}

	public void setTagCompound(NBTTagCompound nbttagcompound) {
		stackTagCompound = nbttagcompound;
	}

	public boolean isLiquidEqual(LiquidStack other) {
		return itemID == other.itemID && itemMeta == other.itemMeta;
	}

	public boolean isLiquidEqual(ItemStack other) {
		return itemID == other.itemID && itemMeta == other.getItemDamage();
	}

	/**
	 * @return An ItemStack representation of this LiquidStack
	 */
	public ItemStack asItemStack() {
		return new ItemStack(itemID, 1, itemMeta);
	}

	/**
	 * Reads a liquid stack from the passed nbttagcompound and returns it.
	 * 
	 * @param nbttagcompound
	 * @return
	 */
	public static LiquidStack loadLiquidStackFromNBT(NBTTagCompound nbttagcompound) {
		LiquidStack liquidstack = new LiquidStack();
		liquidstack.readFromNBT(nbttagcompound);
		return liquidstack.itemID == 0 ? null : liquidstack;
	}

}
