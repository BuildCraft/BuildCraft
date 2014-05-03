/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

import buildcraft.silicon.render.RenderLaserBlock;

public class SiliconProxyClient extends SiliconProxy {
	@Override
	public void registerRenderers() {
		SiliconProxy.laserBlockModel = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(new SiliconRenderBlock());

		ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, new RenderLaserBlock());
	}
}
