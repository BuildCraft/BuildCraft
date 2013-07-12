package buildcraft.silicon;

import buildcraft.silicon.gui.ContainerAdvancedCraftingTable;
import buildcraft.silicon.gui.ContainerAssemblyTable;
import buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		switch (ID) {

		case 0:
			if (!(tile instanceof TileAssemblyTable))
				return null;
			return new GuiAssemblyTable(player.inventory, (TileAssemblyTable) tile);

		case 1:
			if (!(tile instanceof TileAdvancedCraftingTable))
				return null;
			return new GuiAdvancedCraftingTable(player.inventory, (TileAdvancedCraftingTable) tile);
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

		case 0:
			if (!(tile instanceof TileAssemblyTable))
				return null;
			return new ContainerAssemblyTable(player.inventory, (TileAssemblyTable) tile);

		case 1:
			if (!(tile instanceof TileAdvancedCraftingTable))
				return null;
			return new ContainerAdvancedCraftingTable(player.inventory, (TileAdvancedCraftingTable) tile);
		default:
			return null;
		}
	}

}
