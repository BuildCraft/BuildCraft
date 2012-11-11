package buildcraft.silicon;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class SiliconProxyClient extends SiliconProxy {
	@Override
	public void registerRenderers() {
		SiliconProxy.laserBlockModel = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(new SiliconRenderBlock());
	}
}
