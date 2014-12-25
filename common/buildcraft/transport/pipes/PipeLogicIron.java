/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.TileBuffer;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;

public abstract class PipeLogicIron {

	protected final Pipe<?> pipe;
	private boolean lastPower = false;

	public PipeLogicIron(Pipe<?> pipe) {
		this.pipe = pipe;
	}

	public void switchOnRedstone() {
		boolean currentPower = pipe.container.getWorld().isBlockPowered(pipe.container.getPos());

		if (currentPower != lastPower) {
			switchPosition();

			lastPower = currentPower;
		}
	}

	private void switchPosition() {
		int meta = pipe.container.getBlockMetadata();

		for (int i = meta + 1; i <= meta + 6; ++i) {
			EnumFacing facing = EnumFacing.getFront(i % 6);
			if (setFacing(facing)) {
				return;
			}
		}
	}

	private boolean isValidFacing(EnumFacing side) {
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
		lastPower = pipe.container.getWorld().isBlockPowered(pipe.container.getPos());
	}

	public void onBlockPlaced() {
		//pipe.container.getWorld().setBlockMetadataWithNotify(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, 1, 3);
		pipe.container.getWorld().setBlockState(pipe.container.getPos(), pipe.container.getWorld().getBlockState(pipe.container.getPos()).withProperty(BlockGenericPipe.DATA_PROP, EnumFacing.UP.ordinal()), 3);
		switchPosition();
	}

	public boolean setFacing(EnumFacing facing) {
		if (facing.ordinal() != pipe.container.getBlockMetadata() && isValidFacing(facing)) {
			//pipe.container.getWorld().setBlockMetadataWithNotify(pipe.container.getPos(), facing.ordinal(), 3);
			pipe.container.getWorld().setBlockState(pipe.container.getPos(), pipe.container.getWorld().getBlockState(pipe.container.getPos()).withProperty(BlockGenericPipe.DATA_PROP, facing.ordinal()), 3);
			pipe.container.scheduleRenderUpdate();
			return true;
		}
		return false;
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, pipe.container.getPos())) {
			switchPosition();
			pipe.container.scheduleRenderUpdate();
			((IToolWrench) equipped).wrenchUsed(entityplayer, pipe.container.getPos());

			return true;
		}

		return false;
	}

	public EnumFacing getOutputDirection() {
		return EnumFacing.getFront(pipe.container.getBlockMetadata());
	}

	public boolean outputOpen(EnumFacing to) {
		return to.ordinal() == pipe.container.getBlockMetadata();
	}
}
