package buildcraft.factory;

import cpw.mods.fml.common.SidedProxy;

public class FactoryProxy {
	@SidedProxy(clientSide = "buildcraft.factory.FactoryProxyClient", serverSide = "buildcraft.factory.FactoryProxy")
	public static FactoryProxy proxy;

	public void initializeTileEntities() {
	}

	public void initializeEntityRenders() {
	}

	public void initializeNEIIntegration() {
	}
}
