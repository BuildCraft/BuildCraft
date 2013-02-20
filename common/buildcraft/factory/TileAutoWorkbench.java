/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.inventory.TransactorRoundRobin;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.core.utils.CraftingHelper;

public class TileAutoWorkbench extends TileEntity implements ISpecialInventory {

	private ItemStack stackList[] = new ItemStack[9];
	private IRecipe currentRecipe = null;
	
	class LocalInventoryCrafting extends InventoryCrafting {

		public LocalInventoryCrafting() {
			super(new Container() {

				@SuppressWarnings("all")
				public boolean isUsableByPlayer(EntityPlayer entityplayer) {
					return false;
				}

				@SuppressWarnings("all")
				public boolean canInteractWith(EntityPlayer entityplayer) {
					// TODO Auto-generated method stub
					return false;
				}
			}, 3, 3);
			// TODO Auto-generated constructor stub
		}

	}

	public IRecipe getCurrentRecipe() {

		return currentRecipe ;
	}

	@Override
	public int getSizeInventory() {

		return stackList.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return stackList[i];
	}

	@Override
	public ItemStack decrStackSize(int slotId, int count) {
		if (stackList[slotId] == null)
			return null;
		if (stackList[slotId].stackSize > count)
			return stackList[slotId].splitStack(count);
		ItemStack stack = stackList[slotId];
		stackList[slotId] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		stackList[i] = itemstack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (this.stackList[slot] == null)
			return null;

		ItemStack stackToTake = this.stackList[slot];
		this.stackList[slot] = null;
		return stackToTake;
	}

	@Override
	public String getInvName() {

		return "";
	}

	@Override
	public int getInventoryStackLimit() {

		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		Utils.readStacksFromNBT(nbttagcompound, "stackList", stackList);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		Utils.writeStacksToNBT(nbttagcompound, "stackList", stackList);
	}

	class StackPointer {

		IInventory inventory;
		int index;
		ItemStack item;
	}

	public ItemStack findRecipe() {
		InventoryCrafting craftMatrix = new LocalInventoryCrafting();

		for (int i = 0; i < getSizeInventory(); ++i) {
			ItemStack stack = getStackInSlot(i);

			craftMatrix.setInventorySlotContents(i, stack);
		}

		if(this.currentRecipe == null || !this.currentRecipe.matches(craftMatrix, worldObj))
			currentRecipe = CraftingHelper.findMatchingRecipe(craftMatrix, worldObj);

		if(currentRecipe!=null)
			return currentRecipe.getCraftingResult(craftMatrix);
		return null;
	}

	public ItemStack extractItem(boolean doRemove, boolean removeRecipe) {
		InventoryCrafting craftMatrix = new LocalInventoryCrafting();

		LinkedList<StackPointer> pointerList = new LinkedList<StackPointer>();

		int itemsToLeave = (removeRecipe ? 0 : 1);

		for (int i = 0; i < getSizeInventory(); ++i) {
			ItemStack stack = getStackInSlot(i);

			if (stack != null) {
				if (stack.stackSize <= itemsToLeave) {
					StackPointer pointer = getNearbyItem(stack);

					if (pointer == null) {
						resetPointers(pointerList);

						return null;
					} else {
						pointerList.add(pointer);
					}
				} else {
					StackPointer pointer = new StackPointer();
					pointer.inventory = this;
					pointer.item = this.decrStackSize(i, 1);
					pointer.index = i;
					stack = pointer.item;

					pointerList.add(pointer);
				}
			}

			craftMatrix.setInventorySlotContents(i, stack);
		}

		if(this.currentRecipe == null || !this.currentRecipe.matches(craftMatrix, worldObj))
			currentRecipe = buildcraft.core.utils.CraftingHelper.findMatchingRecipe(craftMatrix, worldObj);

		
		ItemStack resultStack = null;
		if(currentRecipe != null) {
			resultStack = currentRecipe.getCraftingResult(craftMatrix);
		}

		if (resultStack == null || !doRemove) {
			resetPointers(pointerList);
		} else {
			for (StackPointer p : pointerList) {
				// replace with the container where appropriate

				if (p.item.getItem().getContainerItem() != null) {
					ItemStack newStack = p.item.getItem().getContainerItemStack(p.item);

					if (p.item.isItemStackDamageable()) {
						if (newStack.getItemDamage() >= p.item.getMaxDamage()) {
							MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(CoreProxy.proxy.getBuildCraftPlayer(worldObj, xCoord, yCoord, zCoord),
									newStack));
							this.worldObj.playSoundAtEntity(CoreProxy.proxy.getBuildCraftPlayer(worldObj, xCoord, yCoord, zCoord), "random.break", 0.8F,
									0.8F + this.worldObj.rand.nextFloat() * 0.4F);
							newStack = null;
						}
					}

					p.inventory.setInventorySlotContents(p.index, newStack);
				}
			}
		}

		return resultStack;
	}

	public void resetPointers(LinkedList<StackPointer> pointers) {
		for (StackPointer p : pointers) {
			ItemStack item = p.inventory.getStackInSlot(p.index);

			if (item == null) {
				p.inventory.setInventorySlotContents(p.index, p.item);
			} else {
				p.inventory.getStackInSlot(p.index).stackSize++;
			}
		}
	}

	public StackPointer getNearbyItem(ItemStack stack) {
		StackPointer pointer = null;

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(stack, ForgeDirection.WEST);
		}

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(stack, ForgeDirection.EAST);
		}

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(stack, ForgeDirection.DOWN);
		}

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(stack, ForgeDirection.UP);
		}

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(stack, ForgeDirection.NORTH);
		}

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(stack, ForgeDirection.SOUTH);
		}

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(stack, ForgeDirection.UNKNOWN);
		}

		return pointer;
	}

	public StackPointer getNearbyItemFromOrientation(ItemStack itemStack, ForgeDirection direction) {
		TileEntity tile = worldObj.getBlockTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);

		if (tile instanceof ISpecialInventory) {
			// Don't get stuff out of ISpecialInventory for now / we wouldn't
			// know how to put it back... And it's not clear if we want to
			// have workbenches automatically getting things from one another.
		} else if (tile instanceof IInventory) {
			IInventory inventory = Utils.getInventory((IInventory) tile);

			for (int j = 0; j < inventory.getSizeInventory(); ++j) {
				ItemStack stack = inventory.getStackInSlot(j);

				if (stack != null) {
					if (stack.stackSize > 0) {
						if (stack.itemID == itemStack.itemID) {
							if (!stack.isItemStackDamageable()) {
								if (stack.itemID == itemStack.itemID && stack.getItemDamage() == itemStack.getItemDamage()) {
									inventory.decrStackSize(j, 1);

									StackPointer result = new StackPointer();
									result.inventory = inventory;
									result.index = j;
									result.item = stack;

									return result;
								}
							} else {
								if (stack.itemID == itemStack.itemID) {
									inventory.decrStackSize(j, 1);

									StackPointer result = new StackPointer();
									result.inventory = inventory;
									result.index = j;
									result.item = stack;

									return result;
								}
							}
						}
					}
				}
			}
		}

		return null;
	}

	public StackPointer getNearbyItem(int itemId, int damage) {
		StackPointer pointer = null;

		pointer = getNearbyItemFromOrientation(itemId, damage, ForgeDirection.WEST);

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(itemId, damage, ForgeDirection.EAST);
		}

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(itemId, damage, ForgeDirection.DOWN);
		}

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(itemId, damage, ForgeDirection.UP);
		}

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(itemId, damage, ForgeDirection.NORTH);
		}

		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(itemId, damage, ForgeDirection.SOUTH);
		}

		return pointer;
	}

	public StackPointer getNearbyItemFromOrientation(int itemId, int damage, ForgeDirection orientation) {
		Position p = new Position(xCoord, yCoord, zCoord, orientation);
		p.moveForwards(1.0);

		TileEntity tile = worldObj.getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);

		if (tile instanceof ISpecialInventory) {
			// Don't get stuff out of ISpecialInventory for now / we wouldn't
			// know how to put it back... And it's not clear if we want to
			// have workbenches automatically getting things from one another.
		} else if (tile instanceof IInventory) {
			IInventory inventory = Utils.getInventory((IInventory) tile);

			for (int j = 0; j < inventory.getSizeInventory(); ++j) {
				ItemStack stack = inventory.getStackInSlot(j);

				if (stack != null && stack.stackSize > 0 && stack.itemID == itemId && stack.getItemDamage() == damage) {
					inventory.decrStackSize(j, 1);

					StackPointer result = new StackPointer();
					result.inventory = inventory;
					result.index = j;
					result.item = stack;

					return result;
				}
			}
		}

		return null;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	/* ISPECIALINVENTORY */
	@Override
	public int addItem(ItemStack stack, boolean doAdd, ForgeDirection from) {
		return new TransactorRoundRobin(this).add(stack, from, doAdd).stackSize;
	}

	@Override
	public ItemStack[] extractItem(boolean doRemove, ForgeDirection from, int maxItemCount) {
		return new ItemStack[] { extractItem(doRemove, false) };
	}

}
