/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.ILiquidContainer;

public class PipeLogicIron extends PipeLogic {

	boolean lastPower = false;
	
	public void switchPower() {
		boolean currentPower = worldObj.isBlockIndirectlyGettingPowered(xCoord,
				yCoord, zCoord);
		
		if (currentPower != lastPower) {
			switchPosition();
			
			lastPower = currentPower;
		}
	}
	
	public void switchPosition() {
		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		
		int nextMetadata = metadata;
		
		for (int l = 0; l < 6; ++l) {
			nextMetadata ++;
			
			if (nextMetadata > 5) {
				nextMetadata = 0;
			}
			
			Position pos = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[nextMetadata]);
			pos.moveForwards(1.0);
			
			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x,
					(int) pos.y, (int) pos.z);		

			if (tile instanceof TileGenericPipe) {
				if (((TileGenericPipe) tile).pipe.logic instanceof PipeLogicWood) {
					continue;
				}
			}
			
			if (tile instanceof IPipeEntry 
					|| tile instanceof IInventory
					|| tile instanceof ILiquidContainer
					|| tile instanceof TileGenericPipe) {
				
				worldObj.setBlockMetadata(xCoord, yCoord, zCoord, nextMetadata);
				return;
			}
		}
	}
	
	@Override
	public void initialize () {
		super.initialize();
		
		lastPower = worldObj.isBlockIndirectlyGettingPowered(xCoord,
				yCoord, zCoord);
	}
		
	@Override
	public void onBlockPlaced()
    {		
		super.onBlockPlaced();
		
		worldObj.setBlockMetadata(xCoord, yCoord, zCoord, 1);
		switchPosition();
    }
	
	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		super.blockActivated(entityplayer);
		
		if (entityplayer.getCurrentEquippedItem() != null 
				&& entityplayer.getCurrentEquippedItem().getItem() == BuildCraftCore.wrenchItem) {

			switchPosition();
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
			
			return true;
		}
		
		return false;
	}

	@Override
	public void onNeighborBlockChange() {
		super.onNeighborBlockChange();
		
		switchPower();
	}
	
	@Override
	public boolean outputOpen(Orientations to) {
		return to.ordinal() == worldObj
				.getBlockMetadata(xCoord, yCoord, zCoord);
	}
		
}
