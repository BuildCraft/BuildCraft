package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class TransportProxy {

	public static void displayGUIFilter(EntityPlayer entityplayer, TileDiamondPipe tileRooter) {
		ModLoader.getMinecraftInstance().displayGuiScreen(
				new GuiFilter(entityplayer.inventory, tileRooter));
	}
	
	static void obsidianPipePickup (World world, EntityItem item, TileEntity tile) {
		ModLoader.getMinecraftInstance().effectRenderer
		.addEffect(new TileEntityPickupFX(world, item, tile));
	}
	
}
