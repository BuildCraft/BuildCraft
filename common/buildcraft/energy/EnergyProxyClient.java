/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import cpw.mods.fml.client.registry.ClientRegistry;

import buildcraft.energy.render.RenderFlywheel;

public class EnergyProxyClient extends EnergyProxy {

	@Override
	public void registerTileEntities() {
		super.registerTileEntities();
	}

	@Override
	public void registerBlockRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileFlywheel.class, new RenderFlywheel());
	}
}
