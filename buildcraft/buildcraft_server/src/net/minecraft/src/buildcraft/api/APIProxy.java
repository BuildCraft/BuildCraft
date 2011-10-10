/** 
 * Copyright (c) SpaceToad, 2011
 * 
 * This file is part of the BuildCraft API. You have the rights to read, 
 * modify, compile or run this the code without restrictions. In addition, it
 * allowed to redistribute this API as well, either in source or binaries 
 * form, or to integrate it into an other mod.
 */

package net.minecraft.src.buildcraft.api;

import java.util.Random;

import net.minecraft.src.Entity;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;

public class APIProxy {

	public static World getWorld () {
		return ModLoader.getMinecraftServerInstance().getWorldManager(0);
	}
	
	public static boolean isClient (World world) {
		return false;
	}
	
	public static boolean isServerSide () {
		return true;
	}
	
	public static Entity getEntity (World world, int entityId) {
		return null;
	}
	
	public static void storeEntity (World world, Entity entity) {
		world.entityJoinedWorld(entity);
	}

	public static void removeEntity (Entity entity) {
		entity.setEntityDead();		
	}
	
	public static Random createNewRandom (World world) {
		return new Random (world.getRandomSeed());
	}
}
