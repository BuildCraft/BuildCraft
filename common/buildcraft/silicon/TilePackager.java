package buildcraft.silicon;

import java.util.EnumMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import gnu.trove.map.hash.TObjectIntHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.IInvSlot;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.NBTUtils;

public class TilePackager extends TileBuildCraft implements ISidedInventory {
	private class Requirement {
		public final IInventory location;
		public final int slot;

		public Requirement(IInventory location, int slot) {
			this.location = location;
			this.slot = slot;
		}

		public boolean isValid() {
			return location.getSizeInventory() > slot && (!(location instanceof TileEntity) || !((TileEntity) location).isInvalid());
		}

		public ItemStack getStack() {
			return location.getStackInSlot(slot);
		}

		public ItemStack decrStackSize(int amount) {
			return location.decrStackSize(slot, amount);
		}

		@Override
		public boolean equals(Object other) {
			if (other == null || !(other instanceof Requirement)) {
				return false;
			}

			Requirement r = (Requirement) other;

			return r.location.equals(this.location) && r.slot == this.slot;
		}

		@Override
		public int hashCode() {
			return location.hashCode() + (slot * 17);
		}
	}

	// Slot 10 is currently missing. Left in for backwards compat.
	private static final int[] SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11};

	public SimpleInventory inventoryPublic = new SimpleInventory(12, "Packager", 64);
	public SimpleInventory inventoryPattern = new SimpleInventory(9, "Packager", 64);

	private Requirement[] requirements = new Requirement[9];
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

		if ((updateTime++) % 5 == 0 && patternsSet > 0) {
			attemptCrafting(inventoryPublic.getStackInSlot(9));
		}
	}

	private boolean validMissing(Requirement r, int missingCount) {
		ItemStack inputStack = r.getStack();
		if (inputStack != null && inputStack.stackSize >= missingCount) {
			// Check if same type used elsewhere
			for (int j = 0; j < 9; j++) {
				ItemStack comparedStack = inventoryPattern.getStackInSlot(j);
				if (isPatternSlotSet(j) && comparedStack != null && StackHelper.isMatchingItem(inputStack, comparedStack, true, false)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
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

		TObjectIntHashMap<Requirement> reqCounts = new TObjectIntHashMap<Requirement>(9);
		int missingCount = 0;

		int filteredReqsToFulfill = 0;

		// STEP 1: Verify all requirements and nullify ones which don't match.
		// Also, add them to a Multimap so we can know which ones get used how often.
		for (int i = 0; i < 9; i++) {
			if (isPatternSlotSet(i)) {
				ItemStack inputStack = inventoryPattern.getStackInSlot(i);
				if (inputStack != null) {
					filteredReqsToFulfill++;
				} else {
					missingCount++;
					requirements[i] = null;
					continue;
				}

				Requirement r = requirements[i];
				if (r == null) {
					continue;
				}
				if (!r.isValid()) {
					requirements[i] = null;
					continue;
				}
				if (r.getStack() == null) {
					requirements[i] = null;
					continue;
				}
				if (inputStack != null) {
					if (!StackHelper.isMatchingItem(inputStack, r.getStack(), true, false)) {
						requirements[i] = null;
						continue;
					}
				}
				reqCounts.adjustOrPutValue(requirements[i], 1, 1);
				filteredReqsToFulfill--;
			} else {
				requirements[i] = null;
			}
		}

		// STEP 2: Verify that the counts are correct.
		for (Requirement r : reqCounts.keys(new Requirement[reqCounts.size()])) {
			if (r.getStack().stackSize < reqCounts.get(r)) {
				int allowedAmount = 0;
				for (int i = 0; i < 9; i++) {
					if (requirements[i] != null && requirements[i].equals(r)) {
						allowedAmount--;
						if (allowedAmount < 0) {
							requirements[i] = null;
							filteredReqsToFulfill++;
						}
					}
				}
				reqCounts.remove(r);
			}
		}

		// STEP 3: Look for all filtered slots. We also use adjacent inventories for this.
		// STEP 3a: Local
		if (filteredReqsToFulfill > 0) {
			for (int i = 0; i < 9; i++) {
				if (filteredReqsToFulfill == 0) {
					break;
				}
				if (isPatternSlotSet(i) && requirements[i] == null) {
					ItemStack inputStack = inventoryPattern.getStackInSlot(i);
					if (inputStack != null) {
						for (int j = 0; j < 9; j++) {
							ItemStack comparedStack = inventoryPublic.getStackInSlot(j);
							if (comparedStack == null) {
								continue;
							}
							Requirement r = new Requirement(this, j);
							if (comparedStack.stackSize <= reqCounts.get(r)) {
								continue;
							}

							if (StackHelper.isMatchingItem(inputStack, comparedStack, true, false)) {
								requirements[i] = r;
								filteredReqsToFulfill--;
								reqCounts.adjustOrPutValue(r, 1, 1);
								break;
							}
						}
					}
				}
			}
		}

		// STEP 3b: Remote
		Map<ForgeDirection, IInventory> invs = new EnumMap<ForgeDirection, IInventory>(ForgeDirection.class);
		if (filteredReqsToFulfill > 0 || missingCount > 0) {
			for (int i = 2; i < 6; i++) {
				TileEntity neighbor = getTile(ForgeDirection.getOrientation(i));
				if (neighbor instanceof IInventory) {
					invs.put(ForgeDirection.getOrientation(i), (IInventory) neighbor);
				}
			}
		}

		if (filteredReqsToFulfill > 0) {
			for (ForgeDirection dir : invs.keySet()) {
				if (filteredReqsToFulfill == 0) {
					break;
				}
				IInventory inv = invs.get(dir);
				Iterable<IInvSlot> iterator = InventoryIterator.getIterable(inv, dir.getOpposite());
				for (IInvSlot slot : iterator) {
					if (filteredReqsToFulfill == 0) {
						break;
					}
					ItemStack comparedStack = slot.getStackInSlot();
					if (comparedStack == null || !slot.canTakeStackFromSlot(comparedStack)) {
						continue;
					}
					Requirement r = new Requirement(inv, slot.getIndex());
					if (comparedStack.stackSize <= reqCounts.get(r)) {
						continue;
					}

					for (int j = 0; j < 9; j++) {
						ItemStack inputStack = inventoryPattern.getStackInSlot(j);
						if (isPatternSlotSet(j) && requirements[j] == null && inputStack != null) {
							if (StackHelper.isMatchingItem(inputStack, comparedStack, true, false)) {
								filteredReqsToFulfill--;
								requirements[j] = r;
								reqCounts.adjustOrPutValue(r, 1, 1);
								break;
							}
						}
					}
				}
			}
		}

		if (filteredReqsToFulfill > 0) {
			return false;
		}

		// STEP 4: Find a matching missing.
		boolean foundMissing = false;

		if (missingCount > 0) {
			for (int i = 0; i < 9; i++) {
				Requirement r = new Requirement(this, i);
				if (reqCounts.contains(r)) {
					continue;
				}
				if (validMissing(r, missingCount)) {
					foundMissing = true;
					for (int j = 0; j < 9; j++) {
						if (requirements[j] == null && isPatternSlotSet(j) && inventoryPattern.getStackInSlot(j) == null) {
							requirements[j] = r;
						}
					}
					reqCounts.adjustOrPutValue(r, missingCount, missingCount);
					missingCount = 0;
					break;
				}
			}
		}

		if (missingCount > 0) {
			for (ForgeDirection dir : invs.keySet()) {
				if (foundMissing) {
					break;
				}
				IInventory inv = invs.get(dir);
				Iterable<IInvSlot> iterator = InventoryIterator.getIterable(inv, dir.getOpposite());
				for (IInvSlot slot : iterator) {
					if (foundMissing) {
						break;
					}
					Requirement r = new Requirement(inv, slot.getIndex());
					if (reqCounts.contains(r)) {
						continue;
					}
					if (validMissing(r, missingCount)) {
						foundMissing = true;
						for (int j = 0; j < 9; j++) {
							if (requirements[j] == null && isPatternSlotSet(j) && inventoryPattern.getStackInSlot(j) == null) {
								requirements[j] = r;
							}
						}
						reqCounts.adjustOrPutValue(r, missingCount, missingCount);
						missingCount = 0;
						break;
					}
				}
			}
		}

		if (missingCount > 0) {
			return false;
		}

		// STEP 5: Craft and output.
		ItemStack pkg;
		if (input.getItem() instanceof ItemPackage) {
			pkg = input.copy();
		} else {
			pkg = new ItemStack(BuildCraftSilicon.packageItem);
		}
		NBTTagCompound pkgTag = NBTUtils.getItemData(pkg);

		boolean broken = false;

		for (int i = 0; i < 9; i++) {
			if (isPatternSlotSet(i)) {
				if (requirements[i] == null) {
					BCLog.logger.error("(Recipe Packager) At " + xCoord + ", " + yCoord + ", " + zCoord + " requirement " + i + " was null! THIS SHOULD NOT HAPPEN!");
					broken = true;
					continue;
				}
				ItemStack usedStack = requirements[i].decrStackSize(1);
				if (usedStack == null) {
					BCLog.logger.error("(Recipe Packager) At " + xCoord + ", " + yCoord + ", " + zCoord + " stack at slot " + i + " was too small! THIS SHOULD NOT HAPPEN!");
					broken = true;
					continue;
				}
				NBTTagCompound itemTag = new NBTTagCompound();
				usedStack.writeToNBT(itemTag);
				pkgTag.setTag("item" + i, itemTag);
			}
		}

		if (broken) {
			return false;
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
		return inventoryPublic.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventoryPublic.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		return inventoryPublic.decrStackSize(slot, amount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return inventoryPublic.getStackInSlotOnClosing(slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventoryPublic.setInventorySlotContents(slot, stack);
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
		return inventoryPublic.isUseableByPlayer(player);
	}

	@Override
	public void openInventory() {
		inventoryPublic.openInventory();
	}

	@Override
	public void closeInventory() {
		inventoryPublic.closeInventory();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == 9) {
			return stack == null || stack.getItem() == Items.paper || stack.getItem() instanceof ItemPackage;
		}
		return inventoryPublic.isItemValidForSlot(slot, stack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		if (side >= 2) {
			if (slot >= 9) {
				return false;
			}
			ItemStack slotStack = inventoryPublic.getStackInSlot(slot);
			if (StackHelper.canStacksMerge(stack, slotStack)) {
				return true;
			}

			for (int i = 0; i < 9; i++) {
				if (isPatternSlotSet(i)) {
					ItemStack inputStack = inventoryPattern.getStackInSlot(i);
					if (inputStack == null) {
						return true;
					}
					if (StackHelper.isMatchingItem(inputStack, stack, true, false)) {
						return true;
					}
				}
			}
			return false;
		} else {
			return slot == 9;
		}
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot == 11;
	}
}
