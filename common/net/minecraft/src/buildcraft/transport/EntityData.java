package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;

public class EntityData {
	// TODO: Move passive data here too, like position, speed and all?
	boolean toCenter = true;
	public EntityPassiveItem item;

	public Orientations orientation;

	public EntityData (EntityPassiveItem citem, Orientations orientation) {
		item = citem;

		this.orientation = orientation;
	}
}