package buildcraft.energy;

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
		if (!(tile instanceof TileEngine))
			return null;

		TileEngine engine = (TileEngine) tile;

		switch (ID) {

		case GuiIds.ENGINE_IRON:
			return new ContainerEngine(player.inventory, engine);

		case GuiIds.ENGINE_STONE:
			return new ContainerEngine(player.inventory, engine);

		default:
			return null;
		}
	}

}
