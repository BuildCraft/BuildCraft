/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import buildcraft.BuildCraftEnergy;
import buildcraft.builders.urbanism.RenderBoxProvider;
import buildcraft.builders.urbanism.TileUrbanist;
import buildcraft.core.render.RenderingEntityBlocks;
import buildcraft.core.render.RenderingEntityBlocks.EntityRenderIndex;
import buildcraft.energy.render.RenderEnergyEmitter;
import buildcraft.energy.render.RenderEngine;
import cpw.mods.fml.client.registry.ClientRegistry;

public class EnergyProxyClient extends EnergyProxy {

	@Override
	public void registerTileEntities() {
		super.registerTileEntities();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEngine.class, new RenderEngine());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEnergyEmitter.class, new RenderEnergyEmitter());

		ClientRegistry.bindTileEntitySpecialRenderer(TileUrbanist.class, new RenderBoxProvider());
	}

	@Override
	public void registerBlockRenderers() {
		//RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.emitterBlock, 0), new RenderEnergyEmitter());
		RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 0), new RenderEngine(TileEngine.WOOD_TEXTURE));
		RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 1), new RenderEngine(TileEngine.STONE_TEXTURE));
		RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 2), new RenderEngine(TileEngine.IRON_TEXTURE));
		RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 3), new RenderEngine(TileEngine.CREATIVE_TEXTURE));
	}
}
