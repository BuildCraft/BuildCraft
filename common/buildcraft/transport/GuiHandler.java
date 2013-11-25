package buildcraft.transport;

import buildcraft.core.GuiIds;
import buildcraft.core.utils.BCLog;
import buildcraft.transport.gui.ContainerDiamondPipe;
import buildcraft.transport.gui.ContainerEmeraldPipe;
import buildcraft.transport.gui.ContainerFilteredBuffer;
import buildcraft.transport.gui.ContainerGateInterface;
import buildcraft.transport.gui.ContainerEmzuliPipe;
import buildcraft.transport.gui.GuiDiamondPipe;
import buildcraft.transport.gui.GuiEmeraldPipe;
import buildcraft.transport.gui.GuiFilteredBuffer;
import buildcraft.transport.gui.GuiGateInterface;
import buildcraft.transport.gui.GuiEmzuliPipe;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsEmerald;
import buildcraft.transport.pipes.PipeItemsEmzuli;
import cpw.mods.fml.common.network.IGuiHandler;
import java.util.logging.Level;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		try {
			if (!world.blockExists(x, y, z))
				return null;

			TileEntity tile = world.getBlockTileEntity(x, y, z);

			if (tile instanceof TileFilteredBuffer) {
				TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
				return new ContainerFilteredBuffer(player.inventory, filteredBuffer);
			}

			if (!(tile instanceof TileGenericPipe))
				return null;

			TileGenericPipe pipe = (TileGenericPipe) tile;

			if (pipe.pipe == null)
				return null;

			switch (ID) {
				case GuiIds.PIPE_DIAMOND:
					return new ContainerDiamondPipe(player.inventory, (PipeItemsDiamond) pipe.pipe);

				case GuiIds.PIPE_EMERALD_ITEM:
					return new ContainerEmeraldPipe(player.inventory, (PipeItemsEmerald) pipe.pipe);

				case GuiIds.PIPE_LOGEMERALD_ITEM:
					return new ContainerEmzuliPipe(player.inventory, (PipeItemsEmzuli) pipe.pipe);

				case GuiIds.GATES:
					if (pipe.pipe.hasGate())
						return new ContainerGateInterface(player.inventory, pipe.pipe);

				default:
					return null;
			}
		} catch (Exception ex) {
			BCLog.logger.log(Level.SEVERE, "Failed to open GUI", ex);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		try {
			if (!world.blockExists(x, y, z))
				return null;

			TileEntity tile = world.getBlockTileEntity(x, y, z);

			if (tile instanceof TileFilteredBuffer) {
				TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
				return new GuiFilteredBuffer(player.inventory, filteredBuffer);
			}

			if (!(tile instanceof TileGenericPipe))
				return null;

			TileGenericPipe pipe = (TileGenericPipe) tile;

			if (pipe.pipe == null)
				return null;

			switch (ID) {
				case GuiIds.PIPE_DIAMOND:
					return new GuiDiamondPipe(player.inventory, (PipeItemsDiamond) pipe.pipe);

				case GuiIds.PIPE_EMERALD_ITEM:
					return new GuiEmeraldPipe(player.inventory, (PipeItemsEmerald) pipe.pipe);

				case GuiIds.PIPE_LOGEMERALD_ITEM:
					return new GuiEmzuliPipe(player.inventory, (PipeItemsEmzuli) pipe.pipe);

				case GuiIds.GATES:
					if (pipe.pipe.hasGate())
						return new GuiGateInterface(player.inventory, pipe.pipe);

				default:
					return null;
			}
		} catch (Exception ex) {
			BCLog.logger.log(Level.SEVERE, "Failed to open GUI", ex);
		}
		return null;
	}
}
