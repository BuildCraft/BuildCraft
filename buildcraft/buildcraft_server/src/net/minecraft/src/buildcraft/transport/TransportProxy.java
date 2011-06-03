package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.ContainerFurnace;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Packet100OpenWindow;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class TransportProxy {

	public static void displayGUIFilter(EntityPlayer entityplayer, TileDiamondPipe tileRooter) {
        EntityPlayerMP player = (EntityPlayerMP) entityplayer;
//        player.getNextWidowId();
//        player.playerNetServerHandler.sendPacket(new Packet100OpenWindow(player.currentWindowId, 2, tileentityfurnace.getInvName(), tileentityfurnace.getSizeInventory()));
        
        player.currentCraftingInventory = new CraftingInv(player.inventory, tileRooter);
        //player.currentCraftingInventory.windowId = player.currentWindowId;
        player.currentCraftingInventory.onCraftGuiOpened(player);
	}
	
	static void obsidianPipePickup (World world, EntityItem item, TileEntity tile) {
		
	}
	
}
