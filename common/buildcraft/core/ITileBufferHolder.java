/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public interface ITileBufferHolder {

	public void blockRemoved(ForgeDirection from);

	public void blockCreated(ForgeDirection from, int blockID, TileEntity tile);

	public int getBlockId(ForgeDirection to);

	public TileEntity getTile(ForgeDirection to);

}
