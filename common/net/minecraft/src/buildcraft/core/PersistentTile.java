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

public abstract class PersistentTile {

	public TileEntity tile;

	public BlockIndex index;

	public void setTile (TileEntity tile) {
		this.tile = tile;
		index = new BlockIndex(tile.xCoord , tile.yCoord, tile.zCoord);
	}

	public void destroy() {

	}

	public boolean isValid () {
		return tile != null && !tile.isInvalid();
	}

}
