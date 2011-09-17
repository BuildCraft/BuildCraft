package net.minecraft.src.buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;

public interface IPipeTransportItemsHook {
	public LinkedList<Orientations> filterPossibleMovements(LinkedList<Orientations> possibleOrientations, Position pos,
			EntityPassiveItem item);
	
	public void entityEntered(EntityPassiveItem item, Orientations orientation);
	
	public void readjustSpeed (EntityPassiveItem item);
}
