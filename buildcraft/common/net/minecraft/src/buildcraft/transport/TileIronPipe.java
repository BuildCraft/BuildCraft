package net.minecraft.src.buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;

public class TileIronPipe extends TilePipe {

	public TileIronPipe() {
		super();
		
		continuousCurrentModel = false;
	}

	public void doWork() {
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
			
			if ((tile instanceof IPipeEntry && !(tile instanceof TileWoodenPipe))
					|| tile instanceof IInventory) {
				worldObj.setBlockMetadata(xCoord, yCoord, zCoord, nextMetadata);
				return;
			}
		}
	}
	
	@Override
	public LinkedList<Orientations> getPossibleMovements(Position pos,
			EntityPassiveItem item) {
		LinkedList<Orientations> result = new LinkedList<Orientations>();

		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		
		if (metadata != -1) {
			
			Position newPos = new Position(pos);
			newPos.orientation = Orientations.values()[metadata];				
			newPos.moveForwards(1.0);
			
			TileEntity tile = worldObj.getBlockTileEntity((int) newPos.x,
					(int) newPos.y, (int) newPos.z);
			
			if (tile instanceof IPipeEntry || tile instanceof IInventory) {
				result.add(newPos.orientation);
			}
		}
		
		return result;
	}
	
}
