/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy;

import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import buildcraft.BuildCraftEnergy;
import buildcraft.core.fluids.FluidHelper;
import buildcraft.energy.render.RenderEnergyEmitter;
import buildcraft.energy.render.RenderEngine;

public class EnergyProxyClient extends EnergyProxy {

	@Override
	public void registerTileEntities() {
		super.registerTileEntities();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEngine.class, new RenderEngine());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEnergyEmitter.class, new RenderEnergyEmitter());
	}

	@Override
	public void registerBlockRenderers() {
		// RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.emitterBlock, 0), new
		// RenderEnergyEmitter());
		// TODO Update me to grab differing trunk textures
		/* RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 0), new
		 * RenderEngine(TileEngine.BASE_TEXTURES[0], TileEngine.CHAMBER_TEXTURES[0], TileEngine.TRUNK_BLUE_TEXTURE));
		 * RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 1), new
		 * RenderEngine(TileEngine.BASE_TEXTURES[1], TileEngine.CHAMBER_TEXTURES[1], TileEngine.TRUNK_BLUE_TEXTURE));
		 * RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 2), new
		 * RenderEngine(TileEngine.BASE_TEXTURES[2], TileEngine.CHAMBER_TEXTURES[2], TileEngine.TRUNK_BLUE_TEXTURE));
		 * RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 3), new
		 * RenderEngine(TileEngine.BASE_TEXTURES[3], TileEngine.CHAMBER_TEXTURES[3], TileEngine.TRUNK_TEXTURES[3])); */

		StateMap ignoreLevel = new StateMap.Builder().addPropertiesToIgnore(FluidHelper.LEVEL).build();
		if (BuildCraftEnergy.blockFuel != null)
			ModelLoader.setCustomStateMapper(BuildCraftEnergy.blockFuel, ignoreLevel);
		if (BuildCraftEnergy.blockOil != null)
			ModelLoader.setCustomStateMapper(BuildCraftEnergy.blockOil, ignoreLevel);
	}
}
