package net.minecraft.src.buildcraft;

import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;

public class TileIronPipe extends TilePipe {

	protected Orientations resolveDestination (EntityData data) {
		int metadata = world.getBlockMetadata(xCoord, yCoord, zCoord);
		
		if (metadata != 0) {
			Position pos = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[metadata]);
			pos.moveForwards(1.0);
			
			TileEntity tile = world.getBlockTileEntity((int) pos.i,
					(int) pos.j, (int) pos.k);
			
			if (tile instanceof IPipeEntry || tile instanceof TileEntityChest) {
				return pos.orientation;
			}
		}
		
		return Orientations.Unknown;
	}
	
}
