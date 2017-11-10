/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.silicon.TileChargingTable;

public class GuiChargingTable extends GuiLaserTable {

	public static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftsilicon:textures/gui/charging_table.png");

	public GuiChargingTable(InventoryPlayer playerInventory, TileChargingTable chargingTable) {
		super(playerInventory, new ContainerChargingTable(playerInventory, chargingTable), chargingTable, TEXTURE);
		xSize = 176;
		ySize = 132;
	}
}
