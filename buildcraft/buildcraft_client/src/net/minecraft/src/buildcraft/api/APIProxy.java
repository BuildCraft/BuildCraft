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

import net.minecraft.src.Entity;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.WorldClient;

public class APIProxy {

	public static World getWorld () {
		return ModLoader.getMinecraftInstance().theWorld;
	}
	
	public static boolean isClient (World world) {
		return world instanceof WorldClient;
	}
	
	public static boolean isServerSide () {
		return false;
	}
	
	public static Entity getEntity (World world, int entityId) {
		if (world instanceof WorldClient) {
			return ((WorldClient) world).func_709_b(entityId);
		} else {
			return null;
		}
	}
	
	public static void storeEntity (World world, Entity entity) {
		if (world instanceof WorldClient) {
			((WorldClient) world).func_712_a(entity.entityId, entity);
		} else {
			world.entityJoinedWorld(entity);
		}
	}

	public static void removeEntity (Entity entity) {
		entity.setEntityDead();
		
		if (entity.worldObj instanceof WorldClient) {
			((WorldClient) entity.worldObj).removeEntityFromWorld(entity.entityId);
		}
	}
	
}
