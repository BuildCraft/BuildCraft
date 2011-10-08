/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 * 
 * As a special exception, this file is part of the BuildCraft API and is 
 * allowed to be redistributed, either in source or binaries form.
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
