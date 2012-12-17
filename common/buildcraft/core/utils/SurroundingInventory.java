/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.utils;

import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.core.IBuilderInventory;

public class SurroundingInventory implements IInventory, IBuilderInventory {

	LinkedList<IInventory> invs = new LinkedList<IInventory>();
	int invSize = 0;

	int x, y, z;
	World world;

	public SurroundingInventory(World world, int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;

		updateInvs();
	}

	public void updateInvs() {
		invs.clear();
		invSize = 0;

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof IInventory) {
			IInventory inv = Utils.getInventory((IInventory) tile);
			invs.add(inv);
			invSize += inv.getSizeInventory();
		}

		Position pos = new Position(x, y, z);

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			tile = Utils.getTile(world, pos, o);

			if (tile instanceof IInventory) {
				IInventory inv = Utils.getInventory((IInventory) tile);
				invs.add(inv);
				invSize += inv.getSizeInventory();
			}
		}
	}

	@Override
	public int getSizeInventory() {
		return invSize;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		int lastSize = 0, size = 0;

		for (IInventory inv : invs) {
			size += inv.getSizeInventory();

			if (size > i)
				return inv.getStackInSlot(i - lastSize);

			lastSize = size;
		}

		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		int lastSize = 0, size = 0;

		for (IInventory inv : invs) {
			size += inv.getSizeInventory();

			if (size > i)
				return inv.decrStackSize(i - lastSize, j);

			lastSize = size;
		}

		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		int lastSize = 0, size = 0;

		for (IInventory inv : invs) {
			size += inv.getSizeInventory();

			if (size > i) {
				inv.setInventorySlotContents(i - lastSize, itemstack);
				break;
			}

			lastSize = size;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		int lastSize = 0, size = 0;

		for (IInventory inv : invs) {
			size += inv.getSizeInventory();

			if (size > slot)
				return inv.getStackInSlotOnClosing(slot - lastSize);
			lastSize = size;
		}

		return null;
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
	public void onInventoryChanged() {
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public boolean isBuildingMaterial(int i) {
		int lastSize = 0, size = 0;

		for (IInventory inv : invs) {
			size += inv.getSizeInventory();

			if (size > i)
				if (inv instanceof IBuilderInventory)
					return ((IBuilderInventory) inv).isBuildingMaterial(i - lastSize);
				else
					return true;

			lastSize = size;
		}

		return false;
	}
}
