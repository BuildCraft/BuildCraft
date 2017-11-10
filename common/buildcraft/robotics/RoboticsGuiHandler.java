/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;

import buildcraft.core.GuiIds;
import buildcraft.robotics.gui.ContainerRequester;
import buildcraft.robotics.gui.ContainerZonePlan;
import buildcraft.robotics.gui.GuiRequester;
import buildcraft.robotics.gui.GuiZonePlan;

public class RoboticsGuiHandler implements IGuiHandler {
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z)) {
			return null;
		}

		TileEntity tile = world.getTileEntity(x, y, z);

		switch (id) {
			case GuiIds.MAP:
				if (!(tile instanceof TileZonePlan)) {
					return null;
				}
				return new GuiZonePlan(player.inventory, (TileZonePlan) tile);

			case GuiIds.REQUESTER:
				if (!(tile instanceof TileRequester)) {
					return null;
				}
				return new GuiRequester(player.inventory, (TileRequester) tile);

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
			case GuiIds.MAP:
				if (!(tile instanceof TileZonePlan)) {
					return null;
				} else {
					return new ContainerZonePlan(player.inventory, (TileZonePlan) tile);
				}

			case GuiIds.REQUESTER:
				if (!(tile instanceof TileRequester)) {
					return null;
				} else {
					return new ContainerRequester(player.inventory, (TileRequester) tile);
				}

			default:
				return null;
		}
	}
}
