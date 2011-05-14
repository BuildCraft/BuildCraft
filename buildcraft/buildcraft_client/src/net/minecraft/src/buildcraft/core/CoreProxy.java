package net.minecraft.src.buildcraft.core;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityItem;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;

public class CoreProxy {
	public static void addName(Object obj, String s) {
		ModLoader.AddName(obj, "Wooden Gear");
	}	
	
	public static World getWorld () {
		return ModLoader.getMinecraftInstance().theWorld;
	}
	
	public static void setField804 (EntityItem item, float value) {
		item.field_804_d = value;
	}
	
	public static File getPropertyFile() {
		return new File(Minecraft.getMinecraftDir(),
				"/config/BuildCraft.cfg");
	}

}
