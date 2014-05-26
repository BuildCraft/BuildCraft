/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import buildcraft.api.boards.IRedstoneBoard;
import buildcraft.core.DefaultProps;
import buildcraft.core.robots.boards.BoardRobotPicker;

public class EntityRobotPicker extends EntityRobot implements IInventory {

	private static ResourceLocation texture = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_picker.png");

	ItemStack[] inv = new ItemStack[6];

	private IRedstoneBoard<EntityRobotPicker> board;

	public EntityRobotPicker(World par1World) {
		super(par1World);

		board = new BoardRobotPicker();
	}

	@Override
	public ResourceLocation getTexture () {
		return texture;
	}

	@Override
	public void onUpdate () {
		super.onUpdate();

		board.updateBoard(this);
	}

	@Override
	public int getSizeInventory() {
		return inv.length;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return inv [var1];
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		ItemStack result = inv [var1].splitStack(var2);

		if (inv [var1].stackSize == 0) {
			inv [var1] = null;
		}

		return result;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return inv [var1].splitStack(var1);
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		inv [var1] = var2;
	}

	@Override
	public String getInventoryName() {
		return null;
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
	public void markDirty() {
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return false;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return inv[var1] == null
				|| (inv[var1].isItemEqual(var2) && inv[var1].isStackable() && inv[var1].stackSize
						+ var2.stackSize <= inv[var1].getItem()
						.getItemStackLimit());
	}

	@Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		for (int i = 0; i < inv.length; ++i) {
			NBTTagCompound stackNbt = new NBTTagCompound();

			if (inv [i] != null) {
				nbt.setTag("inv[" + i + "]", inv [i].writeToNBT(stackNbt));
			}
		}
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		for (int i = 0; i < inv.length; ++i) {
			inv [i] = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("inv[" + i + "]"));
		}

		setDead();
	}
}
