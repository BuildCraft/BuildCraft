package buildcraft.silicon;

import java.lang.ref.WeakReference;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.FMLCommonHandler;

import buildcraft.api.tiles.IHasWork;
import buildcraft.core.lib.gui.ContainerDummy;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.CraftingUtils;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;

public class TileStampingTable extends TileLaserTableBase implements IHasWork, ISidedInventory {
	private class LocalInventoryCrafting extends InventoryCrafting {
		public LocalInventoryCrafting() {
			super(new ContainerDummy(), 3, 3);
		}

		private IRecipe findRecipe() {
			return CraftingUtils.findMatchingRecipe(this, worldObj);
		}
	}

	private static final int[] SLOTS = Utils.createSlotArray(0, 5);
	private SlotCrafting craftSlot;
	private final LocalInventoryCrafting crafting = new LocalInventoryCrafting();

	@Override
	public boolean canUpdate() {
		return !FMLCommonHandler.instance().getEffectiveSide().isClient();
	}

	public WeakReference<EntityPlayer> getInternalPlayer() {
		return CoreProxy.proxy.getBuildCraftPlayer((WorldServer) worldObj, xCoord, yCoord + 1, zCoord);
	}

	private void handleLeftoverItems(IInventory items) {
		for (int i = 0; i < items.getSizeInventory(); i++) {
			if (items.getStackInSlot(i) != null) {
				ItemStack output = items.getStackInSlot(i);

				if (output.stackSize <= 0) {
					items.setInventorySlotContents(i, null);
					continue;
				}

				boolean inserted = false;

				for (int j = 2; j <= 4; j++) {
					ItemStack target = getStackInSlot(j);

					if (target == null || target.stackSize <= 0) {
						setInventorySlotContents(j, output);
						inserted = true;
						break;
					} else {
						output.stackSize -= StackHelper.mergeStacks(output, target, true);
						if (output.stackSize == 0) {
							inserted = true;
							break;
						}
					}
				}

				if (!inserted) {
					if (output.stackSize > 0) {
						output.stackSize -= Utils.addToRandomInventoryAround(worldObj, xCoord, yCoord, zCoord, output);

						if (output.stackSize > 0) {
							InvUtils.dropItems(worldObj, output, xCoord, yCoord + 1, zCoord);
						}
					}
				}

				items.setInventorySlotContents(i, null);
			}
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (getEnergy() >= getRequiredEnergy() && getEnergy() > 0) {
			ItemStack input = this.getStackInSlot(0);

			if (input == null) {
				return;
			}

			EntityPlayer internalPlayer = getInternalPlayer().get();

			if (craftSlot == null) {
				craftSlot = new SlotCrafting(internalPlayer, crafting, this, 1, 0, 0);
			}

			if (input.getItem() instanceof ItemPackage) {
				// Try a recipe made out of the package's contents
				NBTTagCompound tag = NBTUtils.getItemData(input);
				for (int i = 0; i < 9; i++) {
					if (tag.hasKey("item" + i)) {
						ItemStack is = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("item" + i));
						if (is != null) {
							crafting.setInventorySlotContents(i, is);
						} else {
							return;
						}
					} else {
						crafting.setInventorySlotContents(i, null);
					}
				}
			} else {
				// Try a shapeless recipe made from just that item
				ItemStack input2 = input.copy();
				input2.stackSize = 1;
				crafting.setInventorySlotContents(0, input2);
				for (int i = 1; i < 9; i++) {
					crafting.setInventorySlotContents(i, null);
				}
			}

			IRecipe recipe = crafting.findRecipe();
			ItemStack result = recipe != null ? recipe.getCraftingResult(crafting).copy() : null;

			addEnergy(-getRequiredEnergy());

			if (result != null) {
				craftSlot.onPickupFromSlot(internalPlayer, result);
				handleLeftoverItems(crafting);
				handleLeftoverItems(internalPlayer.inventory);

				for (int i = 1; i <= 4; i++) {
					ItemStack inside = inv.getStackInSlot(i);

					if (inside == null || inside.stackSize <= 0) {
						inv.setInventorySlotContents(i, result.copy());
						result.stackSize = 0;
						break;
					} else if (StackHelper.canStacksMerge(inside, result)) {
						result.stackSize -= StackHelper.mergeStacks(result, inside, true);

						if (result.stackSize == 0) {
							break;
						}
					}
				}

				if (result.stackSize > 0) {
					EntityItem entityitem = new EntityItem(worldObj, xCoord + 0.5, yCoord + 0.7, zCoord + 0.5,
							result.copy());

					worldObj.spawnEntityInWorld(entityitem);
					result.stackSize = 0;
				}
				decrStackSize(0, 1);
			} else {
				ItemStack outputSlot = getStackInSlot(1);
				if (outputSlot == null) {
					setInventorySlotContents(1, getStackInSlot(0));
					setInventorySlotContents(0, null);
				}
			}
		}
	}

	@Override
	public int getRequiredEnergy() {
		ItemStack stack = this.getStackInSlot(0);
		ItemStack output = this.getStackInSlot(1);
		if (output != null && output.stackSize == output.getMaxStackSize()) {
			return 0;
		}
		if (stack != null && stack.getItem() != null) {
			if (stack.getItem() instanceof ItemPackage) {
				// tagMap size
				return 400 * NBTUtils.getItemData(stack).func_150296_c().size();
			} else {
				return 400;
			}
		}

		return 0;
	}

	@Override
	public boolean hasWork() {
		return getRequiredEnergy() > 0;
	}

	@Override
	public boolean canCraft() {
		return hasWork();
	}

	@Override
	public int getSizeInventory() {
		return 5;
	}

	@Override
	public String getInventoryName() {
		return StringUtils.localize("tile.stampingTableBlock.name");
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return slot == 0;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		return slot == 0;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot >= 1;
	}
}
