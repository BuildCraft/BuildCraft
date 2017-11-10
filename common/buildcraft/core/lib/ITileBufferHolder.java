/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

public interface ITileBufferHolder {

	void blockRemoved(ForgeDirection from);

	void blockCreated(ForgeDirection from, Block block, TileEntity tile);

	Block getBlock(ForgeDirection to);

	TileEntity getTile(ForgeDirection to);

}
