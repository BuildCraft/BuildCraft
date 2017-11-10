/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.world.World;

import cpw.mods.fml.common.SidedProxy;

import buildcraft.core.lib.EntityBlock;

public class FactoryProxy {
	@SidedProxy(clientSide = "buildcraft.factory.FactoryProxyClient", serverSide = "buildcraft.factory.FactoryProxy")
	public static FactoryProxy proxy;

	public void initializeTileEntities() {
	}

	public void initializeEntityRenders() {
	}

	public EntityBlock newPumpTube(World w) {
		return new EntityBlock(w);
	}
}
