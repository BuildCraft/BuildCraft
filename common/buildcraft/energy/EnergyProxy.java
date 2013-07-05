package buildcraft.energy;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;

public class EnergyProxy {
	@SidedProxy(clientSide = "buildcraft.energy.EnergyProxyClient", serverSide = "buildcraft.energy.EnergyProxy")
	public static EnergyProxy proxy;

	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEngineLegacy.class, "net.minecraft.src.buildcraft.energy.Engine");
		GameRegistry.registerTileEntity(TileEngineWood.class, "net.minecraft.src.buildcraft.energy.TileEngineWood");
		GameRegistry.registerTileEntity(TileEngineStone.class, "net.minecraft.src.buildcraft.energy.TileEngineStone");
		GameRegistry.registerTileEntity(TileEngineIron.class, "net.minecraft.src.buildcraft.energy.TileEngineIron");
	}

	public void registerBlockRenderers() {
	}
}
