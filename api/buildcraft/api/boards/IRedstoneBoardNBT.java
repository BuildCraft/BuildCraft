/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.boards;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

public interface IRedstoneBoardNBT {

	String getID();

	String getName(NBTTagCompound nbt);

	IRedstoneBoard create(NBTTagCompound nbt);

	IIcon getIcon(NBTTagCompound nbt);
}
