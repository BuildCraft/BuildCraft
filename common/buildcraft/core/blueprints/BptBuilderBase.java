/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.blueprints;

import buildcraft.api.core.IAreaProvider;
import buildcraft.core.Box;
import buildcraft.core.IBuilderInventory;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public abstract class BptBuilderBase implements IAreaProvider {

	public BptBase bluePrint;
	int x, y, z;
	public boolean done;
	protected BptContext context;

	public BptBuilderBase(BptBase bluePrint, World world, int x, int y, int z) {
		this.bluePrint = bluePrint;
		this.x = x;
		this.y = y;
		this.z = z;
		done = false;

		Box box = new Box();
		box.initialize(this);

		if (bluePrint instanceof BptBlueprint) {
			context = new BptContext(world, (BptBlueprint) bluePrint, box);
		} else {
			context = new BptContext(world, null, box);
		}
	}

	public abstract BptSlot getNextBlock(World world, IBuilderInventory inv);

	@Override
	public int xMin() {
		return x - bluePrint.anchorX;
	}

	@Override
	public int yMin() {
		return y - bluePrint.anchorY;
	}

	@Override
	public int zMin() {
		return z - bluePrint.anchorZ;
	}

	@Override
	public int xMax() {
		return x + bluePrint.sizeX - bluePrint.anchorX - 1;
	}

	@Override
	public int yMax() {
		return y + bluePrint.sizeY - bluePrint.anchorY - 1;
	}

	@Override
	public int zMax() {
		return z + bluePrint.sizeZ - bluePrint.anchorZ - 1;
	}

	@Override
	public void removeFromWorld() {

	}

	public AxisAlignedBB getBoundingBox() {
		return AxisAlignedBB.getBoundingBox(xMin(), yMin(), zMin(), xMax(), yMax(), zMax());
	}

	public void postProcessing(World world) {

	}

	public BptContext getContext() {
		return context;
	}
}
