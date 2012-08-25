package buildcraft.transport;

import cpw.mods.fml.common.network.IGuiHandler;
import buildcraft.core.GuiIds;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.gui.ContainerDiamondPipe;
import buildcraft.transport.gui.ContainerGateInterface;
import buildcraft.transport.gui.GuiDiamondPipe;
import buildcraft.transport.gui.GuiGateInterface;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,	int x, int y, int z) {
		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileGenericPipe))
			return null;

		TileGenericPipe pipe = (TileGenericPipe) tile;

		switch (ID) {

		case GuiIds.PIPE_DIAMOND:
			return new ContainerDiamondPipe(player.inventory, (PipeLogicDiamond)pipe.pipe.logic);

		case GuiIds.GATES:
			return new ContainerGateInterface(player.inventory, pipe.pipe);

		default:
			return null;
		}
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,	int x, int y, int z) {
		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileGenericPipe))
			return null;

		TileGenericPipe pipe = (TileGenericPipe) tile;

		switch (ID) {

		case GuiIds.PIPE_DIAMOND:
			return new GuiDiamondPipe(player.inventory, pipe);

		case GuiIds.GATES:
			return new GuiGateInterface(player.inventory, pipe.pipe);

		default:
			return null;
		}
	}
}
