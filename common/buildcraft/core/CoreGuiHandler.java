/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;

import buildcraft.core.list.ContainerListNew;
import buildcraft.core.list.ContainerListOld;
import buildcraft.core.list.GuiListNew;
import buildcraft.core.list.GuiListOld;

public class CoreGuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == GuiIds.LIST_OLD) {
			return new GuiListOld(player);
		} else if (id == GuiIds.LIST_NEW) {
			return new GuiListNew(player);
		}
		return null;
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == GuiIds.LIST_OLD) {
			return new ContainerListOld(player);
		} else if (id == GuiIds.LIST_NEW) {
			return new ContainerListNew(player);
		}
		return null;
	}
}
