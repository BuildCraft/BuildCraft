/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.GuiIds;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.gui.*;
import buildcraft.transport.pipes.PipeFluidsEmerald;
import buildcraft.transport.pipes.PipeItemsEmerald;
import buildcraft.transport.pipes.PipeItemsEmzuli;

public class TransportGuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        try {
            if (!world.isBlockLoaded(pos)) {
                return null;
            }

            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof TileFilteredBuffer) {
                TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                return new ContainerFilteredBuffer(player, filteredBuffer);
            }

            if (!(tile instanceof IPipeTile)) {
                return null;
            }

            IPipeTile pipe = (IPipeTile) tile;

            if (pipe.getPipe() == null) {
                return null;
            }

            switch (id) {
                case GuiIds.PIPE_DIAMOND:
                    return new ContainerDiamondPipe(player, (IDiamondPipe) pipe.getPipe());

                case GuiIds.PIPE_EMERALD_ITEM:
                    return new ContainerEmeraldPipe(player, (PipeItemsEmerald) pipe.getPipe());

                case GuiIds.PIPE_LOGEMERALD_ITEM:
                    return new ContainerEmzuliPipe(player, (PipeItemsEmzuli) pipe.getPipe());

                case GuiIds.PIPE_EMERALD_FLUID:
                    return new ContainerEmeraldFluidPipe(player, (PipeFluidsEmerald) pipe.getPipe());

                case GuiIds.GATES:
                    return new ContainerGateInterface(player, pipe.getPipe());

                default:
                    return null;
            }
        } catch (Exception ex) {
            BCLog.logger.log(Level.ERROR, "Failed to open GUI", ex);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        try {
            if (!world.isBlockLoaded(pos)) {
                return null;
            }

            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof TileFilteredBuffer) {
                TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                return new GuiFilteredBufferOld(player, filteredBuffer);
            }

            if (!(tile instanceof IPipeTile)) {
                return null;
            }

            IPipeTile pipe = (IPipeTile) tile;

            if (pipe.getPipe() == null) {
                return null;
            }

            switch (id) {
                case GuiIds.PIPE_DIAMOND:
                    return new GuiDiamondPipe(player, (IDiamondPipe) pipe.getPipe());

                case GuiIds.PIPE_EMERALD_ITEM:
                    return new GuiEmeraldPipe(player, (PipeItemsEmerald) pipe.getPipe());

                case GuiIds.PIPE_LOGEMERALD_ITEM:
                    return new GuiEmzuliPipe(player, (PipeItemsEmzuli) pipe.getPipe());

                case GuiIds.PIPE_EMERALD_FLUID:
                    return new GuiEmeraldFluidPipe(player, (PipeFluidsEmerald) pipe.getPipe());

                case GuiIds.GATES:
                    return new GuiGateInterface(player, pipe.getPipe());

                default:
                    return null;
            }
        } catch (Exception ex) {
            BCLog.logger.log(Level.ERROR, "Failed to open GUI", ex);
        }
        return null;
    }
}
