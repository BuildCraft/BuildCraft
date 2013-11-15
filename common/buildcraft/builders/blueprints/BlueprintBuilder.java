/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import buildcraft.api.builder.BlockHandler;
import buildcraft.api.core.IAreaProvider;
import buildcraft.builders.blueprints.BlueprintBuilder.SchematicBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import static net.minecraftforge.common.ForgeDirection.EAST;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BlueprintBuilder implements IAreaProvider {

	public final Blueprint blueprint;
	public final ForgeDirection orientation;
	public final World worldObj;
	public final int x, y, z;
	private final IInventory inv;
	private final LinkedList<Schematic> buildList;
	private final List<SchematicBuilder> builders;

	public BlueprintBuilder(Blueprint blueprint, World world, int x, int y, int z, ForgeDirection orientation, IInventory inv) {
		this.blueprint = blueprint;
		this.orientation = orientation;
		this.worldObj = world;
		this.x = translateX(x, -blueprint.anchorX, -blueprint.anchorZ);
		this.y = y - blueprint.anchorY;
		this.z = translateZ(z, -blueprint.anchorX, -blueprint.anchorZ);
		this.inv = inv;
		this.buildList = blueprint.getBuildList();
		builders = new ArrayList<SchematicBuilder>(buildList.size());
		for (Schematic schematic : buildList) {
			BlockHandler handler = schematic.getHandler();
			if (handler != null)
				builders.add(new SchematicBuilder(schematic, handler));
		}
	}

	public List<SchematicBuilder> getBuilders() {
		return Collections.unmodifiableList(builders);
	}

	@Override
	public int xMin() {
		return x;
	}

	@Override
	public int yMin() {
		return y;
	}

	@Override
	public int zMin() {
		return z;
	}

	@Override
	public int xMax() {
		return translateX(x, blueprint.sizeX - 1, blueprint.sizeZ - 1);
	}

	@Override
	public int yMax() {
		return y + blueprint.sizeY - 1;
	}

	@Override
	public int zMax() {
		return translateZ(z, blueprint.sizeX - 1, blueprint.sizeZ - 1);
	}

	@Override
	public void removeFromWorld() {
	}

	private int translateX(int corner, int x, int z) {
		switch (orientation) {
			case SOUTH:
				return corner - x;
			case EAST:
				return corner - z;
			case WEST:
				return corner + z;
			default:
				return corner + x;
		}
	}

	private int translateZ(int corner, int x, int z) {
		switch (orientation) {
			case SOUTH:
				return corner - z;
			case EAST:
				return corner + x;
			case WEST:
				return corner - x;
			default:
				return corner + z;
		}
	}

	public class SchematicBuilder {

		public final Schematic schematic;
		public final BlockHandler handler;
		private boolean complete;

		private SchematicBuilder(Schematic schematic, BlockHandler handler) {
			this.schematic = schematic;
			this.handler = handler;
		}

		public int getX() {
			return translateX(x, schematic.x, schematic.z);
		}

		public int getY() {
			return y + schematic.y;
		}

		public int getZ() {
			return translateZ(z, schematic.x, schematic.z);
		}

		public boolean blockExists() {
			return handler.doesBlockMatchSchematic(worldObj, getX(), getY(), getZ(), orientation, schematic.data);
		}

		public boolean canBuild() {
			return handler.canPlaceNow(worldObj, getX(), getY(), getZ(), orientation, schematic.data);
		}

		public boolean build(EntityPlayer bcPlayer) {
//			if (blockExists()) {
//				markComplete();
//				return false;
//			}

//			if (!BlockUtil.canChangeBlock(worldObj, getX(), getY(), getZ()))
//				return false;

			if (!canBuild())
				return false;

			boolean built = handler.readBlockFromSchematic(worldObj, getX(), getY(), getZ(), orientation, schematic.data, inv, bcPlayer);

			if (built) {
				markComplete();
			}

			return built;
		}

		public boolean isComplete() {
			return complete;
		}

		public void markComplete() {
			complete = true;
		}
	}
}
