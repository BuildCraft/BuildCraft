/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

import net.minecraft.src.World;


public interface IBox {

	public Position p1 ();
	
	public Position p2 ();
	
	public void createLasers (World world, LaserKind kind);
	
	public void deleteLasers ();
	
}
