/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import buildcraft.api.core.Orientations;
import buildcraft.api.transport.IPipedItem;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public abstract class PipeTransport {

	public int xCoord;
	public int yCoord;
	public int zCoord;
	public World worldObj;
	public TileGenericPipe container;

	public void setPosition(int xCoord, int yCoord, int zCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}

	public void setWorld(World worldObj) {
		this.worldObj = worldObj;
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

	public boolean isPipeConnected(TileEntity tile) {
		return true;
	}

	public void onNeighborBlockChange(int blockId) {

	}

	public void onBlockPlaced() {

	}

	public void initialize() {

	}

	public boolean inputOpen(Orientations from) {
		return true;
	}

	public boolean outputOpen(Orientations to) {
		return true;
	}

	public boolean acceptItems() {
		return false;
	}

	public void entityEntering(IPipedItem item, Orientations orientation) {

	}

	public void dropContents() {

	}

	public boolean allowsConnect(PipeTransport with) {
		return false;
	}
}
