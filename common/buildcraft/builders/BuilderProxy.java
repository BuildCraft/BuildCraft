/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.world.World;

import cpw.mods.fml.common.SidedProxy;

import buildcraft.core.lib.EntityBlock;

public class BuilderProxy {
	@SidedProxy(clientSide = "buildcraft.builders.BuilderProxyClient", serverSide = "buildcraft.builders.BuilderProxy")
	public static BuilderProxy proxy;
	public static int frameRenderId;

	public void registerClientHook() {

	}

	public void registerBlockRenderers() {

	}

	public EntityBlock newDrill(World w, double i, double j, double k,
								double l, double d, double e, boolean xz) {
		return new EntityBlock(w, i, j, k, l, d, e);
	}

	public EntityBlock newDrillHead(World w, double i, double j, double k,
									double l, double d, double e) {
		return new EntityBlock(w, i, j, k, l, d, e);
	}
}
