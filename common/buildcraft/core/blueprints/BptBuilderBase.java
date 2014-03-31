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
import buildcraft.core.Box;
import buildcraft.core.IBuilderInventory;

public abstract class BptBuilderBase implements IAreaProvider {

	public BlueprintBase blueprint;
	int x, y, z;
	protected boolean done;
	public BptContext context;

	private ArrayList <IBuilder> buildersInAction = new ArrayList<IBuilder>();

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

	public abstract BuildingSlot getNextBlock(World world, IBuilderInventory inv);

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

	public void registerBuilder (IBuilder builder) {
		buildersInAction.add(builder);
	}

	public void removeDoneBuilders () {
		for (int i = buildersInAction.size() - 1; i >= 0; --i) {
			if (buildersInAction.get(i).isDone()) {
				buildersInAction.remove(i);
			}
		}
	}

	public boolean isDone () {
		return done && buildersInAction.size() == 0;
	}
}
