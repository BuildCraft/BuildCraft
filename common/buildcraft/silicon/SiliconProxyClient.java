package buildcraft.silicon;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class SiliconProxyClient extends SiliconProxy {
	public static int laserBlockModel;

	@Override
	public void registerRenderers() {
		laserBlockModel = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(new SiliconRenderBlock());
	}
}
