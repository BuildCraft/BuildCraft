/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import buildcraft.api.boards.IRedstoneBoard;
import buildcraft.api.boards.IRedstoneBoardNBT;

public class BoardRobotPickerNBT implements IRedstoneBoardNBT {

	public IIcon icon;

	@Override
	public String getID() {
		return "buildcraft:boardRobotPicker";
	}

	@Override
	public String getName(NBTTagCompound nbt) {
		return getID();
	}

	@Override
	public IRedstoneBoard create(NBTTagCompound nbt) {
		return new BoardRobotPicker();
	}

	@Override
	public IIcon getIcon(NBTTagCompound nbt) {
		if (icon == null) {
			icon = Minecraft.getMinecraft().getTextureMapBlocks().registerIcon("buildcraft:board_green");
		}

		return icon;
	}

}
