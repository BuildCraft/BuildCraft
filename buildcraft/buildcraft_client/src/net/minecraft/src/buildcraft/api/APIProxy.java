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
