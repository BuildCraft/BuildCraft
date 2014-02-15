/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import buildcraft.silicon.gui.ContainerAdvancedCraftingTable;
import buildcraft.silicon.gui.ContainerAssemblyTable;
import buildcraft.silicon.gui.ContainerIntegrationTable;
import buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import buildcraft.silicon.gui.GuiIntegrationTable;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getTileEntity(x, y, z);

		switch (ID) {

			case 0:
				if (!(tile instanceof TileAssemblyTable))
					return null;
				return new GuiAssemblyTable(player.inventory, (TileAssemblyTable) tile);

			case 1:
				if (!(tile instanceof TileAdvancedCraftingTable))
					return null;
				return new GuiAdvancedCraftingTable(player.inventory, (TileAdvancedCraftingTable) tile);

			case 2:
				if (!(tile instanceof TileIntegrationTable))
					return null;
				return new GuiIntegrationTable(player.inventory, (TileIntegrationTable) tile);
			default:
				return null;
		}
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getTileEntity(x, y, z);

		switch (ID) {

			case 0:
				if (!(tile instanceof TileAssemblyTable))
					return null;
				return new ContainerAssemblyTable(player.inventory, (TileAssemblyTable) tile);

			case 1:
				if (!(tile instanceof TileAdvancedCraftingTable))
					return null;
				return new ContainerAdvancedCraftingTable(player.inventory, (TileAdvancedCraftingTable) tile);

			case 2:
				if (!(tile instanceof TileIntegrationTable))
					return null;
				return new ContainerIntegrationTable(player.inventory, (TileIntegrationTable) tile);
			default:
				return null;
		}
	}
}
