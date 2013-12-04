/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.TileBuffer;
import buildcraft.transport.Pipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public abstract class PipeLogicIron {

	private boolean lastPower = false;
	protected final Pipe pipe;

	public PipeLogicIron(Pipe pipe) {
		this.pipe = pipe;
	}

	public void switchOnRedstone() {
		boolean currentPower = pipe.container.worldObj.isBlockIndirectlyGettingPowered(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);

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
		if (!pipe.container.isPipeConnected(side))
			return false;

		TileBuffer[] tileBuffer = pipe.container.getTileCache();
		if (tileBuffer == null)
			return true;

		if (!tileBuffer[side.ordinal()].exists())
			return true;

		TileEntity tile = tileBuffer[side.ordinal()].getTile();
		return isValidConnectingTile(tile);
	}

	protected abstract boolean isValidConnectingTile(TileEntity tile);

	public void initialize() {
		lastPower = pipe.container.worldObj.isBlockIndirectlyGettingPowered(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
	}

	public void onBlockPlaced() {
		pipe.container.worldObj.setBlockMetadataWithNotify(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, 1, 3);
		switchPosition();
	}

	public boolean setFacing(ForgeDirection facing) {
		if (isValidFacing(facing) && facing.ordinal() != pipe.container.getBlockMetadata()) {
			pipe.container.worldObj.setBlockMetadataWithNotify(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, facing.ordinal(), 3);
			pipe.container.scheduleRenderUpdate();
			return true;
		}
		return false;
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord)) {
			switchPosition();
			pipe.container.scheduleRenderUpdate();
			((IToolWrench) equipped).wrenchUsed(entityplayer, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);

			return true;
		}

		return false;
	}

	public boolean outputOpen(ForgeDirection to) {
		return to.ordinal() == pipe.container.getBlockMetadata();
	}
}
