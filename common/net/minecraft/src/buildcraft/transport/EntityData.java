package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.transport.IPipedItem;

public class EntityData {

	// TODO: Move passive data here too, like position, speed and all?
	boolean toCenter = true;
	public IPipedItem item;

	public Orientations orientation;

	public EntityData(IPipedItem citem, Orientations orientation) {
		item = citem;

		this.orientation = orientation;
	}
}