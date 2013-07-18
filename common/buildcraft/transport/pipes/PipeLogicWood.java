/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeDirection;

public abstract class PipeLogicWood {

	protected final Pipe pipe;

	public PipeLogicWood(Pipe pipe) {
		this.pipe = pipe;
	}

	private void switchSource() {
		int meta = pipe.container.getBlockMetadata();
		ForgeDirection newFacing = null;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (isValidFacing(side)) {
				newFacing = side;
				break;
			}
		}

		if (newFacing != null && newFacing.ordinal() != meta) {
			pipe.container.worldObj.setBlockMetadataWithNotify(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, newFacing.ordinal(), 3);
			pipe.container.scheduleRenderUpdate();
		}
	}

	private void switchSourceIfNeeded() {
		int meta = pipe.container.getBlockMetadata();

		if (meta > 5) {
			switchSource();
		} else {
			ForgeDirection facing = ForgeDirection.getOrientation(meta);
			if (!isValidFacing(facing)) {
				switchSource();
			}
		}
	}

	protected abstract boolean isValidFacing(ForgeDirection facing);

	public void initialize() {
		if (!CoreProxy.proxy.isRenderWorld(pipe.container.worldObj)) {
			switchSourceIfNeeded();
		}
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord)) {
			switchSource();
			((IToolWrench) equipped).wrenchUsed(entityplayer, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
			return true;
		}

		return false;
	}

	public void onNeighborBlockChange(int blockId) {
		if (!CoreProxy.proxy.isRenderWorld(pipe.container.worldObj)) {
			switchSourceIfNeeded();
		}
	}
}
