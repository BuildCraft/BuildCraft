/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;

import buildcraft.BuildCraftTransport;

public class TransportProxy {
	@SidedProxy(clientSide = "buildcraft.transport.TransportProxyClient", serverSide = "buildcraft.transport.TransportProxy")
	public static TransportProxy proxy;
	public static int pipeModel = -1;

	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TileDummyGenericPipe.class, "net.minecraft.src.buildcraft.GenericPipe");
		GameRegistry.registerTileEntity(TileDummyGenericPipe2.class, "net.minecraft.src.buildcraft.transport.TileGenericPipe");
		GameRegistry.registerTileEntity(TileGenericPipe.class, "net.minecraft.src.buildcraft.transport.GenericPipe");
		GameRegistry.registerTileEntity(TileFilteredBuffer.class, "net.minecraft.src.buildcraft.transport.TileFilteredBuffer");
	}

	public void registerRenderers() {
	}

	public void initIconProviders(BuildCraftTransport instance){
		
	}

	public void setIconProviderFromPipe(ItemPipe item, Pipe dummyPipe) {
		
	}

}
