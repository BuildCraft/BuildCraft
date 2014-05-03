/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.world.World;

import cpw.mods.fml.common.SidedProxy;

import buildcraft.core.EntityBlock;

public class FactoryProxy {
	@SidedProxy(clientSide = "buildcraft.factory.FactoryProxyClient", serverSide = "buildcraft.factory.FactoryProxy")
	public static FactoryProxy proxy;

	public void initializeTileEntities() {
	}

	public void initializeEntityRenders() {
	}

	public void initializeNEIIntegration() {
	}

	public EntityBlock newPumpTube(World w) {
        return new EntityBlock(w);
    }

	public EntityBlock newDrill(World w, double i, double j, double k,
			double l, double d, double e) {
        return new EntityBlock(w, i, j, k, l, d, e);
    }

	public EntityBlock newDrillHead(World w, double i, double j, double k,
			double l, double d, double e) {
        return new EntityBlock(w, i, j, k, l, d, e);
    }
}
