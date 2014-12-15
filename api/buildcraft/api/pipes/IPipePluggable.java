/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.pipes;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.INBTStoreable;
import buildcraft.api.core.ISerializable;

/**
 * An IPipePluggable MUST have an empty constructor for client-side
 * rendering!
 */
public interface IPipePluggable extends INBTStoreable, ISerializable {
	ItemStack[] getDropItems(IPipeContainer pipe);

	void onAttachedPipe(IPipeContainer pipe, ForgeDirection direction);

	void onDetachedPipe(IPipeContainer pipe, ForgeDirection direction);

	boolean isBlocking(IPipeContainer pipe, ForgeDirection direction);

	void invalidate();

	void validate(IPipeContainer pipe, ForgeDirection direction);

	AxisAlignedBB getBoundingBox(ForgeDirection side);

	@SideOnly(Side.CLIENT)
	IPipePluggableRenderer getRenderer();
}
