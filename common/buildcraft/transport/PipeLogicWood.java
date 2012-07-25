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
import buildcraft.api.APIProxy;
import buildcraft.api.core.Orientations;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.Utils;
import buildcraft.transport.pipes.PipeLiquidsVoid;
import buildcraft.transport.pipes.PipeLiquidsWood;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.TileEntity;

public class PipeLogicWood extends PipeLogic {

	public static String[] excludedBlocks = new String[0];

	public void switchSource() {
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		int newMeta = 6;

		for (int i = meta + 1; i <= meta + 6; ++i) {
			Orientations o = Orientations.values()[i % 6];

			Block block = Block.blocksList[container.getBlockId(o)];
			TileEntity tile = container.getTile(o);

			if (isInput(tile))
				if (!isExcludedFromExtraction(block)) {
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

	public static boolean isExcludedFromExtraction(Block block) {
		if (block == null)
			return true;

		for (String excluded : excludedBlocks)
			if (excluded.equals(block.getBlockName()) || excluded.equals(Integer.toString(block.blockID)))
				return true;

		return false;
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

		if (!APIProxy.isClient(worldObj))
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

		if (!APIProxy.isClient(worldObj))
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
