/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.GuiIds;
import buildcraft.transport.gui.ContainerDiamondPipe;
import buildcraft.transport.gui.ContainerEmeraldFluidPipe;
import buildcraft.transport.gui.ContainerEmeraldPipe;
import buildcraft.transport.gui.ContainerEmzuliPipe;
import buildcraft.transport.gui.ContainerFilteredBuffer;
import buildcraft.transport.gui.ContainerGateInterface;
import buildcraft.transport.gui.GuiDiamondPipe;
import buildcraft.transport.gui.GuiEmeraldFluidPipe;
import buildcraft.transport.gui.GuiEmeraldPipe;
import buildcraft.transport.gui.GuiEmzuliPipe;
import buildcraft.transport.gui.GuiFilteredBuffer;
import buildcraft.transport.gui.GuiGateInterface;
import buildcraft.transport.pipes.PipeFluidsEmerald;
import buildcraft.transport.pipes.PipeItemsEmerald;
import buildcraft.transport.pipes.PipeItemsEmzuli;

public class TransportGuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		try {
			if (!world.blockExists(x, y, z)) {
				return null;
			}

			TileEntity tile = world.getTileEntity(x, y, z);

			if (tile instanceof TileFilteredBuffer) {
				TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
				return new ContainerFilteredBuffer(player.inventory, filteredBuffer);
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
					return new ContainerDiamondPipe(player.inventory, (IDiamondPipe) pipe.getPipe());

				case GuiIds.PIPE_EMERALD_ITEM:
					return new ContainerEmeraldPipe(player.inventory, (PipeItemsEmerald) pipe.getPipe());

				case GuiIds.PIPE_LOGEMERALD_ITEM:
					return new ContainerEmzuliPipe(player.inventory, (PipeItemsEmzuli) pipe.getPipe());

				case GuiIds.PIPE_EMERALD_FLUID:
					return new ContainerEmeraldFluidPipe(player.inventory, (PipeFluidsEmerald) pipe.getPipe());

				case GuiIds.GATES:
					return new ContainerGateInterface(player.inventory, pipe.getPipe());

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
		try {
			if (!world.blockExists(x, y, z)) {
				return null;
			}

			TileEntity tile = world.getTileEntity(x, y, z);

			if (tile instanceof TileFilteredBuffer) {
				TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
				return new GuiFilteredBuffer(player.inventory, filteredBuffer);
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
					return new GuiDiamondPipe(player.inventory, (IDiamondPipe) pipe.getPipe());

				case GuiIds.PIPE_EMERALD_ITEM:
					return new GuiEmeraldPipe(player.inventory, (PipeItemsEmerald) pipe.getPipe());

				case GuiIds.PIPE_LOGEMERALD_ITEM:
					return new GuiEmzuliPipe(player.inventory, (PipeItemsEmzuli) pipe.getPipe());

				case GuiIds.PIPE_EMERALD_FLUID:
					return new GuiEmeraldFluidPipe(player.inventory, (PipeFluidsEmerald) pipe.getPipe());

				case GuiIds.GATES:
					return new GuiGateInterface(player.inventory, pipe.getPipe());

				default:
					return null;
			}
		} catch (Exception ex) {
			BCLog.logger.log(Level.ERROR, "Failed to open GUI", ex);
		}
		return null;
	}
}
