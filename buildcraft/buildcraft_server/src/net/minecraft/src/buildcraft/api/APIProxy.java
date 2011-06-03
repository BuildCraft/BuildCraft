package net.minecraft.src.buildcraft.api;

import net.minecraft.src.ModLoader;
import net.minecraft.src.World;

public class APIProxy {

	public static World getWorld () {
		return ModLoader.getMinecraftServerInstance().getWorldManager(0);
	}
	
	public static void handlePassiveEntitySpawn (Packet121PassiveItemSpawn packet) {
		
	}
	
	public static void handlePassiveEntityUpdate (Packet122PassiveItemUpdate packet) {
		
	}
	
	public static boolean isClient (World world) {
		return false;
	}
	
	public static boolean isServerSide () {
		return true;
	}
}
