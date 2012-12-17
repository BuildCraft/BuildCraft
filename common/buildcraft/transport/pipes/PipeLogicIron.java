/**
 * Copyright (c) SpaceToad, 2011
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
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;

public class PipeLogicIron extends PipeLogic {

	boolean lastPower = false;

	public void switchPower() {
		boolean currentPower = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

		if (currentPower != lastPower) {
			switchPosition();

			lastPower = currentPower;
		}
	}

	public void switchPosition() {
		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

		int nextMetadata = metadata;

		for (int l = 0; l < 6; ++l) {
			nextMetadata++;

			if (nextMetadata > 5) {
				nextMetadata = 0;
			}

			TileEntity tile = container.getTile(ForgeDirection.values()[nextMetadata]);

			if (tile instanceof TileGenericPipe) {
				Pipe pipe = ((TileGenericPipe) tile).pipe;
				if (pipe.logic instanceof PipeLogicWood || pipe instanceof PipeStructureCobblestone) {
					continue;
				}
			}

			if (tile instanceof IPipeEntry || tile instanceof IInventory || tile instanceof ITankContainer || tile instanceof TileGenericPipe) {

				worldObj.setBlockMetadata(xCoord, yCoord, zCoord, nextMetadata);
				container.scheduleRenderUpdate();
				return;
			}
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		lastPower = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	@Override
	public void onBlockPlaced() {
		super.onBlockPlaced();

		worldObj.setBlockMetadata(xCoord, yCoord, zCoord, 1);
		switchPosition();
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		super.blockActivated(entityplayer);

		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, this.xCoord, this.yCoord, this.zCoord)) {
			switchPosition();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			((IToolWrench) equipped).wrenchUsed(entityplayer, this.xCoord, this.yCoord, this.zCoord);

			return true;
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);

		switchPower();
	}

	@Override
	public boolean outputOpen(ForgeDirection to) {
		return to.ordinal() == worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
	}

}
