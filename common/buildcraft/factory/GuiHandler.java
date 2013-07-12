package buildcraft.factory;

import buildcraft.core.GuiIds;
import buildcraft.factory.gui.ContainerAutoWorkbench;
import buildcraft.factory.gui.ContainerHopper;
import buildcraft.factory.gui.ContainerRefinery;
import buildcraft.factory.gui.GuiAutoCrafting;
import buildcraft.factory.gui.GuiHopper;
import buildcraft.factory.gui.GuiRefinery;
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

		case GuiIds.AUTO_CRAFTING_TABLE:
			if (!(tile instanceof TileAutoWorkbench))
				return null;
			return new GuiAutoCrafting(player.inventory, world, (TileAutoWorkbench) tile);

		case GuiIds.REFINERY:
			if (!(tile instanceof TileRefinery))
				return null;
			return new GuiRefinery(player.inventory, (TileRefinery) tile);

		case GuiIds.HOPPER:
			if (!(tile instanceof TileHopper))
				return null;
			return new GuiHopper(player.inventory, (TileHopper) tile);

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

		case GuiIds.AUTO_CRAFTING_TABLE:
			if (!(tile instanceof TileAutoWorkbench))
				return null;
			return new ContainerAutoWorkbench(player.inventory, (TileAutoWorkbench) tile);

		case GuiIds.REFINERY:
			if (!(tile instanceof TileRefinery))
				return null;
			return new ContainerRefinery(player.inventory, (TileRefinery) tile);

		case GuiIds.HOPPER:
			if (!(tile instanceof TileHopper))
				return null;
			return new ContainerHopper(player.inventory, (TileHopper) tile);

		default:
			return null;
		}
	}

}
