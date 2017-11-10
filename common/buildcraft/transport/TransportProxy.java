/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;

import buildcraft.core.CompatHooks;

public class TransportProxy {

	@SidedProxy(clientSide = "buildcraft.transport.TransportProxyClient", serverSide = "buildcraft.transport.TransportProxy")
	public static TransportProxy proxy;
	public static int pipeModel = -1;

	public void registerTileEntities() {
		// The first name here is the current TE name; the remaining names are old names used for backwards compatibility
		GameRegistry.registerTileEntityWithAlternatives(CompatHooks.INSTANCE.getTile(TileGenericPipe.class), "net.minecraft.src.buildcraft.transport.GenericPipe", "net.minecraft.src.buildcraft.GenericPipe", "net.minecraft.src.buildcraft.transport.TileGenericPipe");
		GameRegistry.registerTileEntity(CompatHooks.INSTANCE.getTile(TileFilteredBuffer.class), "net.minecraft.src.buildcraft.transport.TileFilteredBuffer");
	}

	public void registerRenderers() {
	}

	public void setIconProviderFromPipe(ItemPipe item, Pipe<?> dummyPipe) {
	}

	public void obsidianPipePickup(World world, EntityItem item, TileEntity tile) {
	}

	public void clearDisplayList(int displayList) {
	}
}
