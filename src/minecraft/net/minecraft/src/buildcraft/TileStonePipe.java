package net.minecraft.src.buildcraft;

import java.util.LinkedList;

public class TileStonePipe extends TilePipe {
	
    protected Orientations resolveDestination (EntityData data) {
    	LinkedList<Orientations> listOfPossibleMovements = getPossibleMovements(new Position(
				xCoord, yCoord, zCoord, data.orientation), data.item);
		
		if (listOfPossibleMovements.size() == 0) {					
			return Orientations.Unknown;													
		} else {					
			int i = world.rand.nextInt(listOfPossibleMovements.size());
			
			return listOfPossibleMovements.get(i);															
		}				
    }

}
