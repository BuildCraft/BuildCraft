/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.transport.TileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

/**
 * Note: The entire PipeLogic framework will probably eventually disappear. Use
 * sparingly.
 */
public class PipeLogic {

	public TileGenericPipe container;

	public void setTile(TileGenericPipe tile) {
		this.container = tile;
	}

	/* SAVING & LOADING */
	public void writeToNBT(NBTTagCompound nbttagcompound) {
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
	}

	/* PIPE LOGIC */
	public void initialize() {
	}

	public void updateEntity() {
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		return false;
	}

	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		return true;
	}

	public void onNeighborBlockChange(int blockId) {
	}

	public void onBlockPlaced() {
	}

	public boolean inputOpen(ForgeDirection from) {
		return true;
	}

	public boolean outputOpen(ForgeDirection to) {
		return true;
	}
}
