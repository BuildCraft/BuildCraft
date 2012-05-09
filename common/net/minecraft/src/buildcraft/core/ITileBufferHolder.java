/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.Orientations;

public interface ITileBufferHolder {

	public void blockRemoved (Orientations from);

	public void blockCreated (Orientations from, int blockID, TileEntity tile);

	public int getBlockId (Orientations to);

	public TileEntity getTile (Orientations to);

}
