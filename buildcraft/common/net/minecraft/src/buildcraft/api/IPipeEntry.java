/** 
 * Copyright (c) SpaceToad, 2011
 * 
 * This file is part of the BuildCraft API. You have the rights to read, 
 * modify, compile or run this the code without restrictions. In addition, it
 * allowed to redistribute this API as well, either in source or binaries 
 * form, or to integrate it into an other mod.
 */

package net.minecraft.src.buildcraft.api;


/**
 * Interface used to put objects into pipes, implemented by pipe tile entities.
 */
public interface IPipeEntry {
	
	public void entityEntering(EntityPassiveItem item, Orientations orientation);

	public boolean acceptItems ();
}
