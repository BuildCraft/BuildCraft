package buildcraft.energy;

import buildcraft.energy.render.RenderEngine;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;

public class EnergyProxy {
	@SidedProxy(clientSide = "buildcraft.energy.EnergyProxyClient", serverSide="buildcraft.energy.EnergyProxy")
	public static EnergyProxy proxy;

	public void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TileEngine.class, "net.minecraft.src.buildcraft.energy.Engine");
	}

	public void registerTextureFX() {
	}

	public void registerBlockRenderers() {
	}
}
