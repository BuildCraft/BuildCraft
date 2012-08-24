package buildcraft.energy;

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
		net.minecraft.src.RenderEngine renderEngine = FMLClientHandler.instance().getClient().renderEngine;

		renderEngine.registerTextureFX(new TextureOilFX());
		renderEngine.registerTextureFX(new TextureFuelFX());
		renderEngine.registerTextureFX(new TextureOilFlowFX());
	}
}
