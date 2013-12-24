/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.transport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public abstract class PipeBehavior {

	public final TileEntity tile;

	public PipeBehavior(TileEntity tile) {
		this.tile = tile;
	}

	public void tick() {
	}

	public int getIconIndex(ForgeDirection side) {
		return 0;
	}

	public void writeToNBT(NBTTagCompound nbt) {
	}

	public void readFromNBT(NBTTagCompound nbt) {
	}

	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		return true;
	}

	public boolean blockActivated(EntityPlayer player) {
		return false;
	}

	public void onNeighborBlockChange(int blockId) {
	}
}
