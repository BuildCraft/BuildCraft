package net.minecraft.src.buildcraft.core;

import java.io.File;

import net.minecraft.src.EntityItem;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Packet121PassiveItemSpawn;
import net.minecraft.src.buildcraft.api.Packet122PassiveItemUpdate;

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
