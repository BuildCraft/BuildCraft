/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

public interface ITileBufferHolder {

	void blockRemoved(EnumFacing from);

	void blockCreated(EnumFacing from, IBlockState block, TileEntity tile);

	Block getBlock(EnumFacing to);

	TileEntity getTile(EnumFacing to);

}
