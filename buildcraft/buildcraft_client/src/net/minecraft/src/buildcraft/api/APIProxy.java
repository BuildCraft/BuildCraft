package net.minecraft.src.buildcraft.api;

import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.WorldClient;

public class APIProxy {

	public static World getWorld () {
		return ModLoader.getMinecraftInstance().theWorld;
	}
	
	public static void handlePassiveEntitySpawn (Packet121PassiveItemSpawn packet) {
		EntityPassiveItem entityitem = new EntityPassiveItem(getWorld());
		
		entityitem.setPosition(packet.posX, packet.posY, packet.posZ);
		entityitem.item = new ItemStack(packet.itemID, packet.stackSize,
				packet.damage);
		
		((WorldClient) getWorld()).func_712_a(packet.entityId, entityitem);
	}
	
	public static void handlePassiveEntityUpdate (Packet122PassiveItemUpdate packet) {
		EntityPassiveItem entityitem = (EntityPassiveItem) ((WorldClient) getWorld())
				.func_709_b(packet.entityId);
		
		entityitem.setPosition(packet.posX, packet.posY, packet.posZ);
		entityitem.setVelocity(packet.motionX, packet.motionY, packet.motionZ);
		
	}
	
	public static boolean isClient (World world) {
		return world instanceof WorldClient;
	}

}
