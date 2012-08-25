package buildcraft.energy;

import cpw.mods.fml.common.network.IGuiHandler;
import buildcraft.core.GuiIds;
import buildcraft.energy.TileEngine;
import buildcraft.energy.gui.ContainerEngine;
import buildcraft.energy.gui.GuiCombustionEngine;
import buildcraft.energy.gui.GuiSteamEngine;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileEngine))
			return null;

		TileEngine engine = (TileEngine) tile;

		switch (ID) {

		case GuiIds.ENGINE_IRON:
			return new GuiCombustionEngine(player.inventory, engine);

		case GuiIds.ENGINE_STONE:
			return new GuiSteamEngine(player.inventory, engine);

		default:
			return null;
		}
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {

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
