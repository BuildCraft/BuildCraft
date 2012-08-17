package buildcraft.silicon;

import cpw.mods.fml.common.network.IGuiHandler;
import buildcraft.core.GuiIds;
import buildcraft.factory.TileAssemblyTable;
import buildcraft.silicon.gui.ContainerAssemblyTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		switch (ID) {

		case GuiIds.ASSEMBLY_TABLE:
			if (!(tile instanceof TileAssemblyTable))
				return null;
			return new GuiAssemblyTable(player.inventory, (TileAssemblyTable) tile);

		default:
			return null;
		}
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		switch (ID) {

		case GuiIds.ASSEMBLY_TABLE:
			if (!(tile instanceof TileAssemblyTable))
				return null;
			return new ContainerAssemblyTable(player.inventory, (TileAssemblyTable) tile);

		default:
			return null;
		}
	}

}
