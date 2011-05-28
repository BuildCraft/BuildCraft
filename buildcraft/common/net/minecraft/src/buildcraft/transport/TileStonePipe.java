package net.minecraft.src.buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;

public class TileStonePipe extends TilePipe {
	
    protected Orientations resolveDestination (EntityData data) {
    	LinkedList<Orientations> listOfPossibleMovements = getPossibleMovements(new Position(
				xCoord, yCoord, zCoord, data.orientation), data.item);
		
		if (listOfPossibleMovements.size() == 0) {					
			return Orientations.Unknown;													
		} else {					
			int i = worldObj.rand.nextInt(listOfPossibleMovements.size());
			
			return listOfPossibleMovements.get(i);															
		}				
    }

}
