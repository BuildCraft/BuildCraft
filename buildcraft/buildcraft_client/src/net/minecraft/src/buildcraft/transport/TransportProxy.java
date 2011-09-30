/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;

public class TransportProxy {

	public static void displayGUIFilter(EntityPlayer entityplayer, TileGenericPipe tilePipe) {
		if (!APIProxy.isClient(APIProxy.getWorld())) {
			ModLoader.getMinecraftInstance().displayGuiScreen(
					new GuiDiamondPipe(entityplayer.inventory, tilePipe));
		}
	}
	
	static public void obsidianPipePickup (World world, EntityItem item, TileEntity tile) {
		ModLoader.getMinecraftInstance().effectRenderer
		.addEffect(new TileEntityPickupFX(world, item, tile));
	}
	
}
