/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

public interface IPowerReceptor {
	
	public void setPowerProvider (PowerProvider provider);
	
	public PowerProvider getPowerProvider ();
	
	public void doWork ();
	
	public int powerRequest ();

}
