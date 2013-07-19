/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.api.tools.IToolWrench;
import buildcraft.transport.Pipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeDirection;

public abstract class PipeLogicIron {

	private boolean lastPower = false;
	protected final Pipe pipe;

	public PipeLogicIron(Pipe pipe) {
		this.pipe = pipe;
	}

	private void switchPower() {
		boolean currentPower = pipe.container.worldObj.isBlockIndirectlyGettingPowered(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);

		if (currentPower != lastPower) {
			switchPosition();

			lastPower = currentPower;
		}
	}

	private void switchPosition() {
		int meta = pipe.container.worldObj.getBlockMetadata(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);

		ForgeDirection newFacing = findNextValidDirection(meta);

		if (newFacing.ordinal() != meta) {
			pipe.container.worldObj.setBlockMetadataWithNotify(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, newFacing.ordinal(), 3);
			pipe.container.scheduleRenderUpdate();
		}
	}
	
	private ForgeDirection findNextValidDirection(int oldDirection) {
		// init new direction
		int newDirection = oldDirection + 1;
		// make sure we try every direction only once
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			// start over if you reached the end
			if (newDirection >= ForgeDirection.VALID_DIRECTIONS.length) {
				newDirection = 0;
			}
			// if the facing is valid, return it otherwise continue you search
			if (isValidFacing(ForgeDirection.getOrientation(newDirection))) {
				return ForgeDirection.getOrientation(newDirection);
			} else {
				newDirection++;
			}
		}

		// in case no valid direction is found, return the original one
		return ForgeDirection.getOrientation(oldDirection);
	}

	protected abstract boolean isValidFacing(ForgeDirection facing);

	public void initialize() {
		lastPower = pipe.container.worldObj.isBlockIndirectlyGettingPowered(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
	}

	public void onBlockPlaced() {
		pipe.container.worldObj.setBlockMetadataWithNotify(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, 1, 3);
		switchPosition();
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord)) {
			switchPosition();
			pipe.container.worldObj.markBlockForUpdate(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
			((IToolWrench) equipped).wrenchUsed(entityplayer, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);

			return true;
		}

		return false;
	}

	public void onNeighborBlockChange(int blockId) {
		switchPower();
	}

	public boolean outputOpen(ForgeDirection to) {
		return to.ordinal() == pipe.container.getBlockMetadata();
	}
}
