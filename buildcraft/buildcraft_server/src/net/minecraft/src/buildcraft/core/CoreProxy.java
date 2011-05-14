package net.minecraft.src.buildcraft.core;

import java.io.File;

import net.minecraft.src.EntityItem;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;

public class CoreProxy {
	public static void addName(Object obj, String s) {
		
	}
	
	public static World getWorld () {
		return ModLoader.getMinecraftServerInstance().worldMngr;
	}
	
	public static void setField804 (EntityItem item, float value) {
	
	}
	
	public static File getPropertyFile() {
		return new File("BuildCraft.cfg");
	}

	
}
