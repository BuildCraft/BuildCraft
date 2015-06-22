/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.energy.tile.TileEngineCreative;
import buildcraft.energy.tile.TileEngineIron;
import buildcraft.energy.tile.TileEngineStone;

public class EnergyProxy {
    @SidedProxy(clientSide = "buildcraft.energy.EnergyProxyClient", serverSide = "buildcraft.energy.EnergyProxy")
    public static EnergyProxy proxy;

    public void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEngineStone.class, "buildcraft.energy.engine.stone");
        GameRegistry.registerTileEntity(TileEngineIron.class, "buildcraft.energy.engine.iron");
        GameRegistry.registerTileEntity(TileEngineCreative.class, "buildcraft.energy.engine.creative");
    }

    public void registerBlockRenderers() {}
}
