/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.pipes;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class PipeLogicIron extends PipeLogic {

	private boolean lastPower = false;
	public ForgeDirection direction = ForgeDirection.DOWN;

	public void switchPower() {
		boolean currentPower = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

		if (currentPower != lastPower) {
			switchPosition();

			lastPower = currentPower;
		}
	}

	public void switchPosition() {
		int nextDirection = direction.ordinal();

		for (int l = 0; l < 6; ++l) {
			nextDirection++;

			if (nextDirection > 5)
				nextDirection = 0;

			TileEntity tile = container.getTile(ForgeDirection.values()[nextDirection]);

			if (tile instanceof TileGenericPipe) {
				Pipe pipe = ((TileGenericPipe) tile).pipe;
				if (pipe.logic instanceof PipeLogicWood || pipe instanceof PipeStructureCobblestone)
					continue;
			}

			if (tile instanceof IPipeEntry || tile instanceof IInventory || tile instanceof ITankContainer
					|| tile instanceof TileGenericPipe) {

				direction = ForgeDirection.values()[nextDirection];
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

		switchPosition();
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		super.blockActivated(entityplayer);

		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench
				&& ((IToolWrench) equipped).canWrench(entityplayer, this.xCoord, this.yCoord, this.zCoord)) {
			switchPosition();
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
		return to == direction;
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
