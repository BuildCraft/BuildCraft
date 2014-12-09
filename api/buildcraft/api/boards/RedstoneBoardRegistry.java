/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.boards;

import java.util.Collection;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;

public abstract class RedstoneBoardRegistry {

	public static RedstoneBoardRegistry instance;

	public abstract void registerBoardClass(RedstoneBoardNBT<?> redstoneBoardNBT, float probability);

	public abstract void createRandomBoard(NBTTagCompound nbt);

	public abstract RedstoneBoardNBT getRedstoneBoard(NBTTagCompound nbt);

	public abstract RedstoneBoardNBT<?> getRedstoneBoard(String id);

	public abstract void registerIcons(IIconRegister par1IconRegister);

	public abstract Collection<RedstoneBoardNBT<?>> getAllBoardNBTs();
}
