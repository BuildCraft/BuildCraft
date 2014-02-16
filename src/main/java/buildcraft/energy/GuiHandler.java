/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
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

		TileEntity tile = world.getTileEntity(x, y, z);
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

		TileEntity tile = world.getTileEntity(x, y, z);
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
