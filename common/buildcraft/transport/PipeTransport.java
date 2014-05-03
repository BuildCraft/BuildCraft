/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IPipeTile.PipeType;

public abstract class PipeTransport {

	public TileGenericPipe container;

	public abstract PipeType getPipeType();

	public World getWorld() {
		return container.getWorldObj();
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
	}

	public void updateEntity() {
	}

	public void setTile(TileGenericPipe tile) {
		this.container = tile;
	}

	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		return true;
	}

	public void onNeighborBlockChange(int blockId) {
	}

	public void onBlockPlaced() {
	}

	public void initialize() {
	}

	public boolean inputOpen(ForgeDirection from) {
		return true;
	}

	public boolean outputOpen(ForgeDirection to) {
		return true;
	}

	public void dropContents() {
	}

	public void sendDescriptionPacket() {
	}

	public boolean delveIntoUnloadedChunks() {
		return false;
	}
}
