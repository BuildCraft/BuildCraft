/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.urbanism.RenderBoxProvider;
import cpw.mods.fml.client.registry.ClientRegistry;

public class BuilderProxyClient extends BuilderProxy {

    @Override
	public void registerClientHook() {
		BuildCraftBuilders.addHook(new ClientBuilderHook());
	}

	@Override
	public void registerBlockRenderers() {
		super.registerBlockRenderers();

		ClientRegistry.bindTileEntitySpecialRenderer(TileArchitect.class, new RenderBoxProvider());
	}
}
