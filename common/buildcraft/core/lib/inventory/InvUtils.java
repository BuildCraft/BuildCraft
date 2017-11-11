/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.inventory;

import java.util.Iterator;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.core.lib.utils.BlockUtils;

public final class InvUtils {

	/**
	 * Deactivate constructor
	 */
	private InvUtils() {
	}

	public static int countItems(IInventory inv, ForgeDirection side, IStackFilter filter) {
		int count = 0;
		for (IInvSlot slot : InventoryIterator.getIterable(inv, side)) {
			ItemStack stack = slot.getStackInSlot();
			if (stack != null && filter.matches(stack)) {
				count += stack.stackSize;
			}
		}
		return count;
	}

	public static boolean containsItem(IInventory inv, ForgeDirection side, IStackFilter filter) {
		for (IInvSlot slot : InventoryIterator.getIterable(inv, side)) {
			ItemStack stack = slot.getStackInSlot();
			if (stack != null && filter.matches(stack)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if there is room for the ItemStack in the inventory.
	 *
	 * @param stack The ItemStack
	 * @param dest The IInventory
	 * @return true if room for stack
	 */
	public static boolean isRoomForStack(ItemStack stack, ForgeDirection side, IInventory dest) {
		if (stack == null || dest == null) {
			return false;
		}
		ITransactor tran = Transactor.getTransactorFor(dest);
		return tran.add(stack, side, false).stackSize > 0;
	}

	/**
	 * Attempts to move a single item from one inventory to another.
	 *
	 * @param source
	 * @param dest
	 * @param filter
	 *            an IStackFilter to match against
	 * @return null if nothing was moved, the stack moved otherwise
	 */
	public static ItemStack moveOneItem(IInventory source, ForgeDirection output, IInventory dest, ForgeDirection intput, IStackFilter filter) {
		ITransactor imSource = Transactor.getTransactorFor(source);
		ItemStack stack = imSource.remove(filter, output, false);
		if (stack != null) {
			ITransactor imDest = Transactor.getTransactorFor(dest);
			int moved = imDest.add(stack, intput, true).stackSize;
			if (moved > 0) {
				imSource.remove(filter, output, true);
				return stack;
			}
		}
		return null;
	}

	/* STACK DROPS */
	public static void dropItems(World world, ItemStack stack, int i, int j, int k) {
		if (stack == null || stack.stackSize <= 0) {
			return;
		}

		float f1 = 0.7F;
		double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		EntityItem entityitem = new EntityItem(world, i + d, j + d1, k + d2, stack);
		entityitem.delayBeforeCanPickup = 10;

		world.spawnEntityInWorld(entityitem);
	}

	public static void dropItems(World world, IInventory inv, int i, int j, int k) {
		for (int slot = 0; slot < inv.getSizeInventory(); ++slot) {
			ItemStack items = inv.getStackInSlot(slot);

			if (items != null && items.stackSize > 0) {
				dropItems(world, inv.getStackInSlot(slot).copy(), i, j, k);
			}
		}
	}

	public static void wipeInventory(IInventory inv) {
		for (int slot = 0; slot < inv.getSizeInventory(); ++slot) {
			inv.setInventorySlotContents(slot, null);
		}
	}

	public static NBTTagCompound getItemData(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		return nbt;
	}

	public static void addItemToolTip(ItemStack stack, String msg) {
		NBTTagCompound nbt = getItemData(stack);
		NBTTagCompound display = nbt.getCompoundTag("display");
		nbt.setTag("display", display);
		NBTTagList lore = display.getTagList("Lore", Constants.NBT.TAG_STRING);
		display.setTag("Lore", lore);
		lore.appendTag(new NBTTagString(msg));
	}

	public static void writeInvToNBT(IInventory inv, String tag, NBTTagCompound data) {
		NBTTagList list = new NBTTagList();
		for (byte slot = 0; slot < inv.getSizeInventory(); slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			if (stack != null) {
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", slot);
				stack.writeToNBT(itemTag);
				list.appendTag(itemTag);
			}
		}
		data.setTag(tag, list);
	}

	public static void readInvFromNBT(IInventory inv, String tag, NBTTagCompound data) {
		NBTTagList list = data.getTagList(tag, Constants.NBT.TAG_COMPOUND);
		for (byte entry = 0; entry < list.tagCount(); entry++) {
			NBTTagCompound itemTag = list.getCompoundTagAt(entry);
			int slot = itemTag.getByte("Slot");
			if (slot >= 0 && slot < inv.getSizeInventory()) {
				ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
				inv.setInventorySlotContents(slot, stack);
			}
		}
	}

	public static void readStacksFromNBT(NBTTagCompound nbt, String name, ItemStack[] stacks) {
		NBTTagList nbttaglist = nbt.getTagList(name, Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < stacks.length; ++i) {
			if (i < nbttaglist.tagCount()) {
				NBTTagCompound nbttagcompound2 = nbttaglist.getCompoundTagAt(i);

				stacks[i] = ItemStack.loadItemStackFromNBT(nbttagcompound2);
			} else {
				stacks[i] = null;
			}
		}
	}

	public static void writeStacksToNBT(NBTTagCompound nbt, String name, ItemStack[] stacks) {
		NBTTagList nbttaglist = new NBTTagList();

		for (ItemStack stack : stacks) {
			NBTTagCompound cpt = new NBTTagCompound();
			nbttaglist.appendTag(cpt);
			if (stack != null) {
				stack.writeToNBT(cpt);
			}

		}

		nbt.setTag(name, nbttaglist);
	}

	public static ItemStack consumeItem(ItemStack stack) {
		if (stack.stackSize == 1) {
			if (stack.getItem().hasContainerItem(stack)) {
				return stack.getItem().getContainerItem(stack);
			} else {
				return null;
			}
		} else {
			stack.splitStack(1);

			return stack;
		}
	}

	/**
	 * Ensures that the given inventory is the full inventory, i.e. takes double
	 * chests into account.
	 *
	 * @param inv
	 * @return Modified inventory if double chest, unmodified otherwise.
	 */
	public static IInventory getInventory(IInventory inv) {
		if (inv instanceof TileEntityChest) {
			TileEntityChest adjacent = BlockUtils.getOtherDoubleChest((TileEntityChest) inv);
			if (adjacent != null) {
				return new InventoryLargeChest("", inv, adjacent);
			}
			return inv;
		}
		return inv;
	}

	public static IInvSlot getItem(IInventory inv, IStackFilter filter) {
		for (IInvSlot s : InventoryIterator.getIterable(inv)) {
			if (s.getStackInSlot() != null && filter.matches(s.getStackInSlot())) {
				return s;
			}
		}

		return null;
	}

	public static Iterable<IInvSlot> getItems(final IInventory inv, final IStackFilter filter) {
		return new Iterable<IInvSlot>() {
			@Override
			public Iterator<IInvSlot> iterator() {
				return new Iterator<IInvSlot>() {
					private final Iterator<IInvSlot> parent = InventoryIterator.getIterable(inv).iterator();
					private boolean searched = false;
					private IInvSlot next;

					private void find() {
						next = null;
						searched = true;

						while (parent.hasNext()) {
							IInvSlot s = parent.next();
							if (s.getStackInSlot() != null && filter.matches(s.getStackInSlot())) {
								next = s;
								return;
							}
						}
					}

					@Override
					public boolean hasNext() {
						if (!searched) {
							find();
						}

						return next != null;
					}

					@Override
					public IInvSlot next() {
						if (!searched) {
							find();
						}

						IInvSlot current = next;
						find();
						return current;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Remove not supported.");
					}
				};
			}
		};
	}
}
