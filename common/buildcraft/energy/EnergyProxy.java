/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;

public class EnergyProxy {
	@SidedProxy(clientSide = "buildcraft.energy.EnergyProxyClient", serverSide = "buildcraft.energy.EnergyProxy")
	public static EnergyProxy proxy;

	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEngineStone.class, "net.minecraft.src.buildcraft.energy.TileEngineStone");
		GameRegistry.registerTileEntity(TileEngineIron.class, "net.minecraft.src.buildcraft.energy.TileEngineIron");
		GameRegistry.registerTileEntity(TileEngineCreative.class, "net.minecraft.src.buildcraft.energy.TileEngineCreative");
	}

	public void registerBlockRenderers() {
	}
}
