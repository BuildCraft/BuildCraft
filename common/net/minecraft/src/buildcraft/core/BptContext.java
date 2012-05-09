/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.IBox;
import net.minecraft.src.buildcraft.api.IBptContext;
import net.minecraft.src.buildcraft.api.Position;

public class BptContext implements IBptContext {

	private BptBlueprint bpt;
	private Box box;
	private World world;

	public BptContext (World world, BptBlueprint bpt, Box box) {
		this.bpt = bpt;
		this.box = box;
		this.world = world;
	}

	@Override
	public ItemStack mapItemStack(ItemStack bptItemStack) {
		if (bpt != null)
			return bpt.mapItemStack(bptItemStack);
		else
			return bptItemStack;
	}

	@Override
	public int mapWorldId(int bptWorldId) {
		if (bpt != null)
			return bpt.mapWorldId(bptWorldId);
		else
			return bptWorldId;
	}

	@Override
	public void storeId(int worldId) {
		if (bpt != null)
			bpt.storeId(worldId);
	}

	@Override
	public Position rotatePositionLeft(Position pos) {
		return new Position ((box.sizeZ() - 1) - pos.z, pos.y, pos.x);
	}

	@Override
	public IBox surroundingBox() {
		return box;
	}

	@Override
	public World world() {
		return world;
	}

	public void rotateLeft () {
		box = box.rotateLeft();
	}
}
