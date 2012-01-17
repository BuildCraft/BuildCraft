/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

import java.util.Random;

import net.minecraft.src.Entity;
import net.minecraft.src.World;

public class APIProxy {
	
	public static boolean isClient (World world) {
		return false;
	}
	
	public static boolean isServerSide () {
		return true;
	}

	public static void removeEntity (Entity entity) {
		entity.setEntityDead();		
	}
	
	public static Random createNewRandom (World world) {
		return new Random (world.getRandomSeed());
	}
}
