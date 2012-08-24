package buildcraft.transport;

import cpw.mods.fml.client.registry.ClientRegistry;
import buildcraft.transport.render.RenderPipe;

public class TransportProxyClient extends TransportProxy {
	@Override
	public void registerTileEntities() {
		super.registerTileEntities();
		RenderPipe rp = new RenderPipe();
		ClientRegistry.bindTileEntitySpecialRenderer(TileDummyGenericPipe.class, rp);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDummyGenericPipe2.class, rp);
		ClientRegistry.bindTileEntitySpecialRenderer(TileGenericPipe.class, rp);
	}
}
