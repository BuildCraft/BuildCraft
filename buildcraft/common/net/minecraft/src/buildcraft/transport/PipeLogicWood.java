package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.ILiquidContainer;
import net.minecraft.src.buildcraft.core.Utils;

public class PipeLogicWood extends PipeLogic {

	public static String [] excludedBlocks = new String [0];
	
	public void switchSource () {		
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		int newMeta = 6;
		
		for (int i = meta + 1; i <= meta + 6; ++i) {
			Orientations o = Orientations.values() [i % 6];
			
			Position pos = new Position (xCoord, yCoord, zCoord, o);
			
			pos.moveForwards(1);
			
			Block block = Block.blocksList[worldObj.getBlockId((int) pos.x,
					(int) pos.y, (int) pos.z)];
			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);
			
			if ((tile instanceof IInventory || tile instanceof ILiquidContainer
					&& !(tile instanceof TileGenericPipe))
					&& Utils.checkPipesConnections(worldObj, xCoord, yCoord,
							zCoord, tile.xCoord, tile.yCoord, tile.zCoord)) {
				
				if (!isExcludedFromExtraction(block)) {
					newMeta = o.ordinal();
					break;
				}
			}
		}
		
		if (newMeta != meta) {
			worldObj.setBlockMetadata(xCoord, yCoord, zCoord, newMeta);
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	public static boolean isExcludedFromExtraction (Block block) {
		if (block == null) {
			return true;
		}
		
		for (String excluded : excludedBlocks) {			
			if (excluded.equals (block.getBlockName())
					|| excluded.equals (Integer.toString(block.blockID))) {
				return true;
			}
		}
		
		return false;
	}
	
    
    public boolean blockActivated(EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() != null 
				&& entityplayer.getCurrentEquippedItem().getItem() == BuildCraftCore.wrenchItem) {
			
			switchSource();

			return true;
		}
    	
        return false;
    }        
    
	@Override
	public boolean isPipeConnected(TileEntity tile) {
		Pipe pipe2 = null;

		if (tile instanceof TileGenericPipe) {
			pipe2 = ((TileGenericPipe) tile).pipe;
		}

		if (BuildCraftTransport.alwaysConnectPipes) {
			return super.isPipeConnected(tile);
		} else {
			return (pipe2 == null || !(pipe2.logic instanceof PipeLogicWood))
					&& super.isPipeConnected(tile);
		}
	}
}
