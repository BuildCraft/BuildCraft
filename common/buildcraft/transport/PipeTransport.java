/**
 * Copyright (c) SpaceToad, 2011
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
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipedItem;

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

	public boolean isPipeConnected(TileEntity tile, ForgeDirection side) {
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

	public boolean acceptItems() {
		return false;
	}

	public void entityEntering(IPipedItem item, ForgeDirection orientation) {

	}

	public void dropContents() {

	}

	public boolean allowsConnect(PipeTransport with) {
		return false;
	}

	public void sendDescriptionPacket() {

	}
}
