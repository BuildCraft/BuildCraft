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

	public static final String PIPE_IO_SETTINGS = "iosetting";

	public TileGenericPipe container;

	protected boolean[] inputsOpen = new boolean[ForgeDirection.VALID_DIRECTIONS.length];
	protected boolean[] outputsOpen = new boolean[ForgeDirection.VALID_DIRECTIONS.length];

	public PipeTransport() {
		for (int b = 0; b < ForgeDirection.VALID_DIRECTIONS.length; b++) {
			inputsOpen[b] = true;
			outputsOpen[b] = true;
		}
	}

	public abstract PipeType getPipeType();

	public World getWorld() {
		return container.getWorldObj();
	}

	public void readFromNBT(NBTTagCompound nbt) {
	    if (nbt.hasKey(PIPE_IO_SETTINGS)) {
			int iosettings = nbt.getInteger(PIPE_IO_SETTINGS);
			for (int b = 0; b < ForgeDirection.VALID_DIRECTIONS.length; b++) {
				inputsOpen[b] = (iosettings & (1 << b)) == 1;
				outputsOpen[b] = (iosettings & (1 << (b + 8))) == 1;
			}
	    }
	}

	public void writeToNBT(NBTTagCompound nbt) {
	    int iosettings = 0;

	    for (int b = 0; b < ForgeDirection.VALID_DIRECTIONS.length; b++) {
			if (inputsOpen[b]) {
				iosettings |= 1 << b;
			}

			if (outputsOpen[b]) {
				iosettings |= 1 << (b + 8);
			}
	    }

	    nbt.setInteger(PIPE_IO_SETTINGS, iosettings);
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
	    return inputsOpen[from.ordinal()];
	}

	public boolean outputOpen(ForgeDirection to) {
	    return outputsOpen[to.ordinal()];
	}

	public void allowInput(ForgeDirection from, boolean allow) {
		if (from != ForgeDirection.UNKNOWN) {
			inputsOpen[from.ordinal()] = allow;
		}
	}

	public void allowOutput(ForgeDirection to, boolean allow) {
		if (to != ForgeDirection.UNKNOWN) {
			outputsOpen[to.ordinal()] = allow;
		}
	}

	public void dropContents() {
	}

	public void sendDescriptionPacket() {
	}

	public boolean delveIntoUnloadedChunks() {
		return false;
	}
}
