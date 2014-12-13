/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.util.EnumFacing;

public interface IPipePluggable {
	void writeToNBT(NBTTagCompound nbt);

	void readFromNBT(NBTTagCompound nbt);

	ItemStack[] getDropItems(IPipeTile pipe);

	void onAttachedPipe(IPipeTile pipe, EnumFacing direction);

	void onDetachedPipe(IPipeTile pipe, EnumFacing direction);

	boolean blocking(IPipeTile pipe, EnumFacing direction);

	void invalidate();

	void validate(IPipeTile pipe, EnumFacing direction);
}
