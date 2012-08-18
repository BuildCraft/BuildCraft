/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Orientations;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.ProxyCore;
import buildcraft.core.Utils;
import buildcraft.transport.pipes.PipeLiquidsWood;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.TileEntity;

public class PipeLogicWood extends PipeLogic {

	public void switchSource() {
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		int newMeta = 6;

		for (int i = meta + 1; i <= meta + 6; ++i) {
			Orientations o = Orientations.values()[i % 6];

			TileEntity tile = container.getTile(o);

			if (isInput(tile))
				if (PipeManager.canExtractItems(container.getPipe(), tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord) || PipeManager.canExtractLiquids(container.getPipe(), tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord) ) {
					newMeta = o.ordinal();
					break;
				}
		}

		if (newMeta != meta) {
			worldObj.setBlockMetadata(xCoord, yCoord, zCoord, newMeta);
			container.scheduleRenderUpdate();
			//worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
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

		if (!ProxyCore.proxy.isRemote(worldObj))
			switchSourceIfNeeded();
	}

	private void switchSourceIfNeeded() {
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

		if (meta > 5)
			switchSource();
		else {
			TileEntity tile = container.getTile(Orientations.values()[meta]);

			if (!isInput(tile))
				switchSource();
		}
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);

		if (!ProxyCore.proxy.isRemote(worldObj))
			switchSourceIfNeeded();
	}
	
	@Override
	public boolean outputOpen(Orientations to) {
		if (this.container.pipe instanceof PipeLiquidsWood){
			int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			return meta != to.ordinal();
		}
		return super.outputOpen(to);
	}
}
