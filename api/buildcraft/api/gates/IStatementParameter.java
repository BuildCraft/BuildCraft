/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.gates;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import buildcraft.api.transport.IPipeTile;

public interface IStatementParameter {

	ItemStack getItemStackToDraw();

	IIcon getIconToDraw();

	void clicked(IPipeTile pipe, IStatement stmt, ItemStack stack);

	void writeToNBT(NBTTagCompound compound);

	void readFromNBT(NBTTagCompound compound);

}
