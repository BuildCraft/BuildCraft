package buildcraft.silicon;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.InventoryConcatenator;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;

public class TilePackager extends TileBuildCraft implements ISidedInventory {
	private static final int[] SLOTS = Utils.createSlotArray(0, 12);

	public SimpleInventory inventoryPublic = new SimpleInventory(12, "Packager", 64);
	public SimpleInventory inventoryPattern = new SimpleInventory(9, "Packager", 64);
	public IInventory visibleInventory = InventoryConcatenator.make().add(inventoryPublic).add(inventoryPattern);

	private int patternsSet;
	private int updateTime = BuildCraftCore.random.nextInt(5);

	public boolean isPatternSlotSet(int p) {
		return (patternsSet & (1 << p)) != 0;
	}

	public void setPatternSlot(int p, boolean v) {
		if (v) {
			patternsSet |= 1 << p;
		} else {
			patternsSet &= ~(1 << p);
		}
	}

	@Override
	public void updateEntity() {
		if (worldObj.isRemote) {
			return;
		}

		if ((updateTime++) % 5 == 0) {
			attemptCrafting(inventoryPublic.getStackInSlot(9));
		}
	}

	private boolean attemptCrafting(ItemStack input) {
		// STEP 0: Make sure the conditions are correct.
		if (inventoryPublic.getStackInSlot(11) != null) {
			return false;
		}

		if (input == null || input.stackSize == 0 || !(input.getItem() == Items.paper || input.getItem() instanceof ItemPackage)) {
			return false;
		}

		if (input.getItem() instanceof ItemPackage) {
			NBTTagCompound inputTag = NBTUtils.getItemData(input);

			for (int i = 0; i < 9; i++) {
				if (isPatternSlotSet(i) && inputTag.hasKey("item" + i)) {
					return false;
				}
			}
		}

		// STEP 1: Find how many "missing patterns" we have,
		// and find the first item matching this.
		// Also, match all the non-missing patterns.
		int missingCount = 0;
		int[] bindings = new int[9];
		int[] usedItems = new int[9];

		for (int i = 0; i < 9; i++) {
			if (isPatternSlotSet(i)) {
				ItemStack inputStack = inventoryPattern.getStackInSlot(i);
				if (inputStack == null) {
					missingCount++;
				} else {
					boolean found = false;
					for (int j = 0; j < 9; j++) {
						ItemStack comparedStack = inventoryPublic.getStackInSlot(j);
						if (comparedStack != null && usedItems[j] < comparedStack.stackSize
							&& StackHelper.isMatchingItem(inputStack, comparedStack, true, false)) {
							usedItems[j]++;
							bindings[i] = j;
							found = true;
							break;
						}
					}
					if (!found) {
						return false;
					}
				}
			}
		}

		// STEP 2: If we have any missings, find the first stack
		// which is NOT used elsewhere AND has enough items.
		if (missingCount > 0) {
			int missingPos = -1;
			for (int i = 0; i < 9; i++) {
				if (usedItems[i] == 0) {
					ItemStack comparedStack = inventoryPublic.getStackInSlot(i);
					if (comparedStack != null && comparedStack.stackSize >= missingCount) {
						missingPos = i;
						break;
					}
				}
			}
			if (missingPos < 0) {
				return false;
			} else {
				for (int i = 0; i < 9; i++) {
					if (isPatternSlotSet(i)) {
						ItemStack inputStack = inventoryPattern.getStackInSlot(i);
						if (inputStack == null) {
							bindings[i] = missingPos;
						}
					}
				}
			}
		}

		// STEP 3: Craft and output.
		ItemStack pkg;
		if (input.getItem() instanceof ItemPackage) {
			pkg = input.copy();
		} else {
			pkg = new ItemStack(BuildCraftSilicon.packageItem);
		}
		NBTTagCompound pkgTag = NBTUtils.getItemData(pkg);

		for (int i = 0; i < 9; i++) {
			if (isPatternSlotSet(i)) {
				ItemStack usedStack = inventoryPublic.getStackInSlot(bindings[i]);
				ItemStack output = usedStack.splitStack(1);
				NBTTagCompound itemTag = new NBTTagCompound();
				output.writeToNBT(itemTag);
				pkgTag.setTag("item" + i, itemTag);
			}
		}

		ItemPackage.update(pkg);

		decrStackSize(9, 1);
		setInventorySlotContents(11, pkg);

		return true;
	}

	@Override
	public void readData(ByteBuf stream) {
		super.readData(stream);
		patternsSet = stream.readUnsignedShort();
	}

	@Override
	public void writeData(ByteBuf stream) {
		super.writeData(stream);
		stream.writeShort(patternsSet);
	}

	@Override
	public void readFromNBT(NBTTagCompound cpd) {
		super.readFromNBT(cpd);
		inventoryPublic.readFromNBT(cpd, "items");
		inventoryPattern.readFromNBT(cpd, "pattern");
		patternsSet = cpd.getShort("patternSet");
	}

	@Override
	public void writeToNBT(NBTTagCompound cpd) {
		super.writeToNBT(cpd);
		inventoryPublic.writeToNBT(cpd, "items");
		inventoryPattern.writeToNBT(cpd, "pattern");
		cpd.setShort("patternSet", (short) patternsSet);
	}

	@Override
	public int getSizeInventory() {
		return visibleInventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return visibleInventory.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		return visibleInventory.decrStackSize(slot, amount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return visibleInventory.getStackInSlotOnClosing(slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		visibleInventory.setInventorySlotContents(slot, stack);
	}

	@Override
	public String getInventoryName() {
		return "Packager";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return visibleInventory.isUseableByPlayer(player);
	}

	@Override
	public void openInventory() {
		visibleInventory.openInventory();
	}

	@Override
	public void closeInventory() {
		visibleInventory.closeInventory();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == 9) {
			return (stack == null || stack.getItem() == Items.paper || stack.getItem() instanceof ItemPackage);
		}
		return visibleInventory.isItemValidForSlot(slot, stack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		if (side >= 2) {
			return slot < 9;
		} else {
			return slot == 9;
		}
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot == 11;
	}
}
