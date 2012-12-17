package buildcraft.energy;

import buildcraft.BuildCraftEnergy;
import buildcraft.core.DefaultProps;
import buildcraft.core.render.RenderingEntityBlocks;
import buildcraft.core.render.RenderingEntityBlocks.EntityRenderIndex;
import buildcraft.energy.render.RenderEngine;
import buildcraft.energy.render.TextureFuelFX;
import buildcraft.energy.render.TextureOilFX;
import buildcraft.energy.render.TextureOilFlowFX;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;

public class EnergyProxyClient extends EnergyProxy {
	@Override
	public void registerTileEntities() {
		super.registerTileEntities();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEngine.class, new RenderEngine());
	}

	@Override
	public void registerTextureFX() {
		net.minecraft.client.renderer.RenderEngine renderEngine = FMLClientHandler.instance().getClient().renderEngine;

		renderEngine.registerTextureFX(new TextureOilFX());
		renderEngine.registerTextureFX(new TextureFuelFX());
		renderEngine.registerTextureFX(new TextureOilFlowFX());
	}

	@Override
	public void registerBlockRenderers() {
		RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 0), new RenderEngine(
				DefaultProps.TEXTURE_PATH_BLOCKS + "/base_wood.png"));
		RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 1), new RenderEngine(
				DefaultProps.TEXTURE_PATH_BLOCKS + "/base_stone.png"));
		RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 2), new RenderEngine(
				DefaultProps.TEXTURE_PATH_BLOCKS + "/base_iron.png"));
	}
}
