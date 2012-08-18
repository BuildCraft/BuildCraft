/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import buildcraft.transport.render.TileEntityPickupFX;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.WorldClient;

public class ClientProxyCore extends ProxyCore {

	/* ENTITY HANDLING */
	@Override
	public void removeEntity(Entity entity) {
		super.removeEntity(entity);
	
		if (isRemote(entity.worldObj))
			((WorldClient) entity.worldObj).removeEntityFromWorld(entity.entityId);
	}

	/* LOCALIZATION */
	@Override
	public String getCurrentLanguage() {
		return StringTranslate.getInstance().getCurrentLanguage();
	}
	@Override
	public void addName(Object obj, String s) {
		ModLoader.addName(obj, s);
	}
	@Override
	public void addLocalization(String s1, String string) {
		ModLoader.addLocalization(s1, string);
	}	
	@Override
	public String getItemDisplayName(ItemStack stack){
		if (Item.itemsList[stack.itemID] == null) return "";
		
		return Item.itemsList[stack.itemID].getItemDisplayName(stack);
	}

	/* NETWORKING */
	@Override
	public void sendToServer(Packet packet) {
		ModLoader.getMinecraftInstance().getSendQueue().addToSendQueue(packet);
	}

	/* BUILDCRAFT PLAYER */
	@Override
	public String playerName() {
		return ModLoader.getMinecraftInstance().thePlayer.username;
	}

	private EntityPlayer createNewPlayer(World world) {
		return new EntityPlayer(world) {
			@Override public void sendChatToPlayer(String var1) {}
			@Override public boolean canCommandSenderUseCommand(String var1) { return false; }
		};
	}

	@Override
	public EntityPlayer getBuildCraftPlayer(World world) {
		if (ProxyCore.buildCraftPlayer == null) {
			ProxyCore.buildCraftPlayer = createNewPlayer(world);
		}
	
		return ProxyCore.buildCraftPlayer;
	}

	@Override
	public void obsidianPipePickup(World world, EntityItem item, TileEntity tile) {
		ModLoader.getMinecraftInstance().effectRenderer.addEffect(new TileEntityPickupFX(world, item, tile));
	}
}
