package buildcraft.factory;

import buildcraft.core.GuiIds;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.forge.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

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
