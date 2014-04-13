/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import cpw.mods.fml.common.SidedProxy;

public class BuilderProxy {
	@SidedProxy(clientSide = "buildcraft.builders.BuilderProxyClient", serverSide = "buildcraft.builders.BuilderProxy")
	public static BuilderProxy proxy;

	public void registerClientHook() {

	}

	public void registerBlockRenderers () {

	}
}
