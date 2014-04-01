/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.util.ArrayList;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.Position;
import buildcraft.builders.BuildingItem;
import buildcraft.builders.TileAbstractBuilder;
import buildcraft.core.Box;

public abstract class BptBuilderBase implements IAreaProvider {

	public BlueprintBase blueprint;
	int x, y, z;
	protected boolean done;
	public BptContext context;

	public BptBuilderBase(BlueprintBase bluePrint, World world, int x, int y, int z) {
		this.blueprint = bluePrint;
		this.x = x;
		this.y = y;
		this.z = z;
		done = false;

		Box box = new Box();
		box.initialize(this);

		context = bluePrint.getContext(world, box);
	}

	public abstract BuildingSlot getNextBlock(World world, TileAbstractBuilder inv);

	public boolean buildNextSlot (World world, TileAbstractBuilder builder, int x, int y, int z) {
		BuildingSlot slot = getNextBlock(world, builder);

		if (slot != null) {
			BuildingItem i = new BuildingItem();
			i.origin = new Position (x + 0.5, y + 0.5, z + 0.5);
			i.destination = slot.getDestination();
			i.slotToBuild = slot;
			i.context = getContext();
			i.stacksToBuild = slot.stackConsumed;
			builder.addBuildingItem(i);

			return true;
		}

		return false;
	}

	@Override
	public int xMin() {
		return x - blueprint.anchorX;
	}

	@Override
	public int yMin() {
		return y - blueprint.anchorY;
	}

	@Override
	public int zMin() {
		return z - blueprint.anchorZ;
	}

	@Override
	public int xMax() {
		return x + blueprint.sizeX - blueprint.anchorX - 1;
	}

	@Override
	public int yMax() {
		return y + blueprint.sizeY - blueprint.anchorY - 1;
	}

	@Override
	public int zMax() {
		return z + blueprint.sizeZ - blueprint.anchorZ - 1;
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

	public void removeDoneBuilders (TileAbstractBuilder builder) {
		ArrayList<BuildingItem> items = builder.getBuilders();

		for (int i = items.size() - 1; i >= 0; --i) {
			if (items.get(i).isDone()) {
				items.remove(i);
			}
		}
	}

	public boolean isDone (TileAbstractBuilder builder) {
		return done && builder.getBuilders().size() == 0;
	}
}
