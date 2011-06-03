package net.minecraft.src.buildcraft.core;

import java.io.File;

import net.minecraft.src.EntityItem;

public class CoreProxy {
	public static void addName(Object obj, String s) {
		
	}
	
	public static void setField804 (EntityItem item, float value) {
		item.field_432_ae = value;
	}
	
	public static File getPropertyFile() {
		return new File("BuildCraft.cfg");
	}
	
}
