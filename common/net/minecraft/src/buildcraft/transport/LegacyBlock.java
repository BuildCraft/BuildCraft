/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

public class LegacyBlock extends BlockContainer {

	public int newPipeId;

	public LegacyBlock (int itemId, int newPipeId) {
		super (itemId, Material.glass);
		this.newPipeId = newPipeId;
	}

	@Override
	public TileEntity getBlockEntity() {
		return new LegacyTile();
	}

}
