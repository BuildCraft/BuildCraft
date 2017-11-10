/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.lib.TileBuffer;
import buildcraft.transport.Pipe;

public abstract class PipeLogicIron {

	protected final Pipe<?> pipe;
	private boolean lastPower = false;

	public PipeLogicIron(Pipe<?> pipe) {
		this.pipe = pipe;
	}

	public void switchOnRedstone() {
		boolean currentPower = pipe.container.getWorldObj().isBlockIndirectlyGettingPowered(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);

		if (currentPower != lastPower) {
			switchPosition();

			lastPower = currentPower;
		}
	}

	private void switchPosition() {
		int meta = pipe.container.getBlockMetadata();

		for (int i = meta + 1; i <= meta + 6; ++i) {
			ForgeDirection facing = ForgeDirection.getOrientation(i % 6);
			if (setFacing(facing)) {
				return;
			}
		}
	}

	private boolean isValidFacing(ForgeDirection side) {
		if (!pipe.container.isPipeConnected(side)) {
			return false;
		}

		TileBuffer[] tileBuffer = pipe.container.getTileCache();

		if (tileBuffer == null) {
			return true;
		} else if (!tileBuffer[side.ordinal()].exists()) {
			return true;
		}

		TileEntity tile = tileBuffer[side.ordinal()].getTile();
		return isValidOutputTile(tile);
	}

	protected boolean isValidOutputTile(TileEntity tile) {
		return !(tile instanceof IInventory && ((IInventory) tile).getInventoryStackLimit() == 0) && isValidConnectingTile(tile);
	}

	protected abstract boolean isValidConnectingTile(TileEntity tile);

	public void initialize() {
		lastPower = pipe.container.getWorldObj().isBlockIndirectlyGettingPowered(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
	}

	public void onBlockPlaced() {
		pipe.container.getWorldObj().setBlockMetadataWithNotify(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, 1, 3);
		switchPosition();
	}

	public boolean setFacing(ForgeDirection facing) {
		if (facing.ordinal() != pipe.container.getBlockMetadata() && isValidFacing(facing)) {
			pipe.container.getWorldObj().setBlockMetadataWithNotify(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, facing.ordinal(), 3);
			pipe.container.scheduleRenderUpdate();
			return true;
		}
		return false;
	}

	@Deprecated
	public boolean blockActivated(EntityPlayer entityplayer) {
		return blockActivated(entityplayer, ForgeDirection.UNKNOWN);
	}

	public boolean blockActivated(EntityPlayer entityplayer, ForgeDirection side) {
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord)) {
			if (side == ForgeDirection.UNKNOWN) {
				switchPosition();
			} else {
				setFacing(side);
			}
			pipe.container.scheduleRenderUpdate();
			((IToolWrench) equipped).wrenchUsed(entityplayer, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);

			return true;
		}

		return false;
	}

	public ForgeDirection getOutputDirection() {
		return ForgeDirection.getOrientation(pipe.container.getBlockMetadata());
	}

	public boolean outputOpen(ForgeDirection to) {
		return to.ordinal() == pipe.container.getBlockMetadata();
	}
}
