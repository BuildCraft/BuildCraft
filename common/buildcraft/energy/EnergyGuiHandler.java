/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;

import buildcraft.core.GuiIds;
import buildcraft.core.lib.engines.TileEngineWithInventory;
import buildcraft.energy.gui.ContainerEngine;
import buildcraft.energy.gui.GuiCombustionEngine;
import buildcraft.energy.gui.GuiStoneEngine;

public class EnergyGuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z)) {
			return null;
		}

		TileEntity tile = world.getTileEntity(x, y, z);
		if (!(tile instanceof TileEngineWithInventory)) {
			return null;
		}

		TileEngineWithInventory engine = (TileEngineWithInventory) tile;

		switch (id) {

			case GuiIds.ENGINE_IRON:
				return new GuiCombustionEngine(player.inventory, (TileEngineIron) engine);

			case GuiIds.ENGINE_STONE:
				return new GuiStoneEngine(player.inventory, (TileEngineStone) engine);

			default:
				return null;
		}
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z)) {
			return null;
		}

		TileEntity tile = world.getTileEntity(x, y, z);
		if (!(tile instanceof TileEngineWithInventory)) {
			return null;
		}

		TileEngineWithInventory engine = (TileEngineWithInventory) tile;

		switch (id) {

			case GuiIds.ENGINE_IRON:
				return new ContainerEngine(player.inventory, engine);

			case GuiIds.ENGINE_STONE:
				return new ContainerEngine(player.inventory, engine);

			default:
				return null;
		}
	}

}
