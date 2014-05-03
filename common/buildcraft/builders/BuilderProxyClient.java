/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

import buildcraft.builders.urbanism.RenderBoxProvider;
import buildcraft.core.render.RenderBlockMultiTexture;

public class BuilderProxyClient extends BuilderProxy {

	@Override
	public void registerClientHook() {

	}

	@Override
	public void registerBlockRenderers() {
		super.registerBlockRenderers();

		RenderingRegistry.registerBlockHandler(new RenderBlockMultiTexture());

		ClientRegistry.bindTileEntitySpecialRenderer(TileArchitect.class, new RenderBoxProvider());
		ClientRegistry.bindTileEntitySpecialRenderer(TileFiller.class, new RenderBuilder());
		ClientRegistry.bindTileEntitySpecialRenderer(TileBuilder.class, new RenderBuilder());
		ClientRegistry.bindTileEntitySpecialRenderer(TilePathMarker.class, new RenderPathMarker());
	}
}
