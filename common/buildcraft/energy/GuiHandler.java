package buildcraft.energy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.core.GuiIds;
import buildcraft.energy.gui.ContainerEngine;
import buildcraft.energy.gui.GuiCombustionEngine;
import buildcraft.energy.gui.GuiStoneEngine;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileEngineWithInventory))
			return null;

		TileEngineWithInventory engine = (TileEngineWithInventory) tile;

		switch (ID) {

		case GuiIds.ENGINE_IRON:
			return new GuiCombustionEngine(player.inventory, engine);

		case GuiIds.ENGINE_STONE:
			return new GuiStoneEngine(player.inventory, engine);

		default:
			return null;
		}
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileEngineWithInventory))
			return null;

		TileEngineWithInventory engine = (TileEngineWithInventory) tile;

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
