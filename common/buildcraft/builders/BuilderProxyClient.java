/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import cpw.mods.fml.client.registry.ClientRegistry;

import buildcraft.builders.urbanism.TileUrbanist;
import buildcraft.core.render.RenderBoxProvider;
import buildcraft.core.render.RenderBuilder;

public class BuilderProxyClient extends BuilderProxy {

	@Override
	public void registerClientHook() {

	}

	@Override
	public void registerBlockRenderers() {
		super.registerBlockRenderers();

		ClientRegistry.bindTileEntitySpecialRenderer(TileUrbanist.class, new RenderBoxProvider());
		ClientRegistry.bindTileEntitySpecialRenderer(TileArchitect.class, new RenderArchitect());
		ClientRegistry.bindTileEntitySpecialRenderer(TileFiller.class, new RenderBuilder());
		ClientRegistry.bindTileEntitySpecialRenderer(TileBuilder.class, new RenderBuilder());
		ClientRegistry.bindTileEntitySpecialRenderer(TilePathMarker.class, new RenderPathMarker());
		ClientRegistry.bindTileEntitySpecialRenderer(TileConstructionMarker.class, new RenderConstructionMarker());
	}
}
