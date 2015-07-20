/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import buildcraft.core.GuiIds;
import buildcraft.core.lib.engines.TileEngineWithInventory;
import buildcraft.energy.gui.ContainerEngine;
import buildcraft.energy.gui.GuiCombustionEngine;
import buildcraft.energy.gui.GuiStoneEngine;
import buildcraft.energy.tile.TileEngineIron;
import buildcraft.energy.tile.TileEngineStone;

public class EnergyGuiHandler implements IGuiHandler {

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        BlockPos pos = new BlockPos(x, y, z);
        if (world.isAirBlock(pos)) {
            return null;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEngineWithInventory)) {
            return null;
        }

        TileEngineWithInventory engine = (TileEngineWithInventory) tile;

        switch (id) {

            case GuiIds.ENGINE_IRON:
                return new GuiCombustionEngine(player.inventory, (TileEngineIron) engine);

            case GuiIds.ENGINE_STONE:
                return new GuiStoneEngine(player.inventory, (TileEngineStone) engine);

            default:
                return null;
        }
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        BlockPos pos = new BlockPos(x, y, z);
        if (world.isAirBlock(pos)) {
            return null;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEngineWithInventory)) {
            return null;
        }

        TileEngineWithInventory engine = (TileEngineWithInventory) tile;

        switch (id) {

            case GuiIds.ENGINE_IRON:
                return new ContainerEngine(player.inventory, engine);

            case GuiIds.ENGINE_STONE:
                return new ContainerEngine(player.inventory, engine);

            default:
                return null;
        }
    }

}
