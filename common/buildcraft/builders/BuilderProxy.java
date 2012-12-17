package buildcraft.builders;

import cpw.mods.fml.common.SidedProxy;

public class BuilderProxy {
	@SidedProxy(clientSide = "buildcraft.builders.BuilderProxyClient", serverSide = "buildcraft.builders.BuilderProxy")
	public static BuilderProxy proxy;

	public void registerClientHook() {

	}
}
