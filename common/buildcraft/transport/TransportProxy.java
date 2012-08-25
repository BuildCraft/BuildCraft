package buildcraft.transport;

import buildcraft.mod_BuildCraftTransport;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;

public class TransportProxy {
	@SidedProxy(clientSide = "buildcraft.transport.TransportProxyClient", serverSide = "buildcraft.transport.TransportProxy")
	public static TransportProxy proxy;

	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TileDummyGenericPipe.class, "net.minecraft.src.buildcraft.GenericPipe");
		GameRegistry.registerTileEntity(TileDummyGenericPipe2.class, "net.minecraft.src.buildcraft.transport.TileGenericPipe");
		GameRegistry.registerTileEntity(TileGenericPipe.class, "net.minecraft.src.buildcraft.transport.GenericPipe");
	}

}
