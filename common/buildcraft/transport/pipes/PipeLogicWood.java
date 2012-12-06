/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class PipeLogicWood extends PipeLogic {

	public ForgeDirection direction = ForgeDirection.DOWN;

	public void switchSource() {
		ForgeDirection newDir = ForgeDirection.UNKNOWN;

		for (int i = direction.ordinal() + 1; i <= direction.ordinal() + 6; ++i) {
			ForgeDirection o = ForgeDirection.values()[i % 6];

			TileEntity tile = container.getTile(o);

			if (isInput(tile))
				if (PipeManager.canExtractItems(container.getPipe(), tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord) || PipeManager.canExtractLiquids(container.getPipe(), tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord) ) {
					newDir = o;
					break;
				}
		}

		if (newDir != direction) {
			direction = newDir;
			container.scheduleRenderUpdate();
		}
	}

	public boolean isInput(TileEntity tile) {
		return !(tile instanceof TileGenericPipe) && (tile instanceof IInventory || tile instanceof ITankContainer)
				&& Utils.checkPipesConnections(container, tile);
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench
				&& ((IToolWrench) equipped).canWrench(entityplayer, this.xCoord, this.yCoord, this.zCoord)) {
			switchSource();
			((IToolWrench) equipped).wrenchUsed(entityplayer, this.xCoord, this.yCoord, this.zCoord);
			return true;
		}

		return false;
	}

	@Override
	public boolean isPipeConnected(TileEntity tile) {
		Pipe pipe2 = null;

		if (tile instanceof TileGenericPipe)
			pipe2 = ((TileGenericPipe) tile).pipe;

		if (BuildCraftTransport.alwaysConnectPipes)
			return super.isPipeConnected(tile);
		else
			return (pipe2 == null || !(pipe2.logic instanceof PipeLogicWood)) && super.isPipeConnected(tile);
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!CoreProxy.proxy.isRenderWorld(worldObj))
			switchSourceIfNeeded();
	}

	private void switchSourceIfNeeded() {
		if (direction == ForgeDirection.UNKNOWN)
			switchSource();
		else {
			TileEntity tile = container.getTile(direction);

			if (!isInput(tile))
				switchSource();
		}
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);

		if (!CoreProxy.proxy.isRenderWorld(worldObj))
			switchSourceIfNeeded();
	}

	@Override
	public boolean outputOpen(ForgeDirection to) {
		if (container.pipe instanceof PipeLiquidsWood)
			return direction != to;
		return super.outputOpen(to);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		if (nbttagcompound.hasKey("direction"))
			direction = ForgeDirection.values()[nbttagcompound.getInteger("direction")];
		else
			direction = ForgeDirection.values()[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("direction", direction.ordinal());
	}

}
