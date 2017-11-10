/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;

import buildcraft.core.GuiIds;
import buildcraft.factory.gui.ContainerAutoWorkbench;
import buildcraft.factory.gui.ContainerHopper;
import buildcraft.factory.gui.ContainerRefinery;
import buildcraft.factory.gui.GuiAutoCrafting;
import buildcraft.factory.gui.GuiHopper;
import buildcraft.factory.gui.GuiRefinery;

public class FactoryGuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z)) {
			return null;
		}

		TileEntity tile = world.getTileEntity(x, y, z);

		switch (id) {

			case GuiIds.AUTO_CRAFTING_TABLE:
				if (!(tile instanceof TileAutoWorkbench)) {
					return null;
				} else {
					return new GuiAutoCrafting(player.inventory, world, (TileAutoWorkbench) tile);
				}

			case GuiIds.REFINERY:
				if (!(tile instanceof TileRefinery)) {
					return null;
				} else {
					return new GuiRefinery(player.inventory, (TileRefinery) tile);
				}

			case GuiIds.HOPPER:
				if (!(tile instanceof TileHopper)) {
					return null;
				} else {
					return new GuiHopper(player.inventory, (TileHopper) tile);
				}

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

		switch (id) {

			case GuiIds.AUTO_CRAFTING_TABLE:
				if (!(tile instanceof TileAutoWorkbench)) {
					return null;
				} else {
					return new ContainerAutoWorkbench(player.inventory, (TileAutoWorkbench) tile);
				}

			case GuiIds.REFINERY:
				if (!(tile instanceof TileRefinery)) {
					return null;
				} else {
					return new ContainerRefinery(player.inventory, (TileRefinery) tile);
				}

			case GuiIds.HOPPER:
				if (!(tile instanceof TileHopper)) {
					return null;
				} else {
					return new ContainerHopper(player.inventory, (TileHopper) tile);
				}

			default:
				return null;
		}
	}

}
