/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;

public class FactoryProxy {

	public static void displayGUIAutoCrafting (World world, EntityPlayer entityplayer, int i, int j, int k) {
		if (!APIProxy.isClient(APIProxy.getWorld())) {
			ModLoader.getMinecraftInstance().displayGuiScreen(
					new GuiAutoCrafting(entityplayer.inventory, world,
							(TileAutoWorkbench) world.getBlockTileEntity(i, j, k)));
		}
	}
	
}
