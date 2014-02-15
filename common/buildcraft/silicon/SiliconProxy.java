/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import cpw.mods.fml.common.SidedProxy;

public class SiliconProxy {
	@SidedProxy(clientSide = "buildcraft.silicon.SiliconProxyClient", serverSide = "buildcraft.silicon.SiliconProxy")
	public static SiliconProxy proxy;
	public static int laserBlockModel = -1;

	public void registerRenderers() {
	}

}
