/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import net.minecraft.src.EntityItem;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class TransportProxy {

	static public void obsidianPipePickup(World world, EntityItem item, TileEntity tile) {
		ModLoader.getMinecraftInstance().effectRenderer.addEffect(new TileEntityPickupFX(world, item, tile));
	}

}
