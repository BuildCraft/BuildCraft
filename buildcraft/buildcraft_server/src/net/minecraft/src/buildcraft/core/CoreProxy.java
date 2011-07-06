package net.minecraft.src.buildcraft.core;

import java.io.File;

import net.minecraft.src.BaseModMp;
import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.Item;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ModLoaderMp;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.EntityPlayerMP;

public class CoreProxy {
	public static void addName(Object obj, String s) {
		
	}
	
	public static void setField804 (EntityItem item, float value) {
		item.field_432_ae = value;
	}
	
	public static File getPropertyFile() {
		return new File("BuildCraft.cfg");
	}

	public static void sendToPlayers(Packet230ModLoader packet, int x, int y,
			int z, int maxDistance, BaseModMp mod) {
		if (APIProxy.isServerSide()) {
			for (int i = 0; i < ModLoader.getMinecraftServerInstance().worldMngr.length; i++) {
				for (int j = 0; j < ModLoader.getMinecraftServerInstance().worldMngr[i].playerEntities
						.size(); j++) {
					EntityPlayerMP player = (EntityPlayerMP) ModLoader
							.getMinecraftServerInstance().worldMngr[i].playerEntities
							.get(j);

					if (Math.abs(player.posX - x) <= maxDistance
							&& Math.abs(player.posY - y) <= maxDistance
							&& Math.abs(player.posZ - z) <= maxDistance) {
						ModLoaderMp.SendPacketTo(mod, player, packet);
					}
				}

			}
		}
	}
	
	public static boolean isPlainBlock (Block block) {
		return block.func_28025_b();
	}

	public static File getBuildCraftBase() {
		return new File("buildcraft/");
	}

	public static void addLocalization(String s1, String string) {
		// TODO Auto-generated method stub
		
	}
	
	public static boolean isDamageable (Item item) {
		return item.func_25005_e();
	}
	
}
