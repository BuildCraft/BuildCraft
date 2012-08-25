package buildcraft.silicon;

import cpw.mods.fml.common.SidedProxy;

public class SiliconProxy {
	@SidedProxy(clientSide = "buildcraft.silicon.SiliconProxyClient", serverSide = "buildcraft.silicon.SiliconProxy")
	public static SiliconProxy proxy;

	public void registerRenderers() {
	}



}
