/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.builder;

import buildcraft.api.builder.BlueprintBuilder.SchematicBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import static net.minecraftforge.common.ForgeDirection.EAST;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BlueprintBuilder {

	public final Blueprint blueprint;
	public final ForgeDirection orientation;
	public final World worldObj;
	public final int x, y, z;
	private final IInventory inv;
	private final LinkedList<BlockSchematic> buildList;
	private final List<SchematicBuilder> builders;

	public BlueprintBuilder(Blueprint blueprint, World world, int x, int y, int z, ForgeDirection orientation, IInventory inv) {
		this.blueprint = blueprint;
		this.orientation = orientation;
		this.worldObj = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.inv = inv;
		this.buildList = blueprint.getBuildList();
		builders = new ArrayList<SchematicBuilder>(buildList.size());
		for (BlockSchematic schematic : buildList) {
			builders.add(new SchematicBuilder(schematic));
		}
	}

	public List<SchematicBuilder> getBuilders() {
		return Collections.unmodifiableList(builders);
	}

	public class SchematicBuilder {

		public final BlockSchematic schematic;
		public final BlockHandler handler;
		private boolean complete;

		private SchematicBuilder(BlockSchematic schematic) {
			this.schematic = schematic;
			this.handler = BlockHandler.getHandler(schematic);
		}

		public int getX() {
			switch (orientation) {
				case SOUTH:
					return x - schematic.x;
				case EAST:
					return x - schematic.z;
				case WEST:
					return x + schematic.z;
				default:
					return x + schematic.x;
			}
		}

		public int getY() {
			return y + schematic.y;
		}

		public int getZ() {
			switch (orientation) {
				case SOUTH:
					return z - schematic.z;
				case EAST:
					return z + schematic.x;
				case WEST:
					return z - schematic.x;
				default:
					return z + schematic.z;
			}
		}

		public boolean blockExists() {
			return handler.doesBlockMatchSchematic(worldObj, getX(), getY(), getZ(), orientation, schematic);
		}

		public boolean canBuild() {
			return handler.canPlaceNow(worldObj, getX(), getY(), getZ(), orientation, schematic);
		}

		public boolean build() {
//			if (blockExists()) {
//				markComplete();
//				return false;
//			}

//			if (!BlockUtil.canChangeBlock(worldObj, getX(), getY(), getZ()))
//				return false;

			if (!canBuild())
				return false;

			if (inv != null && !handler.consumeItems(schematic, inv)) {
				return false;
			}

			boolean built = handler.readBlockFromSchematic(worldObj, getX(), getY(), getZ(), orientation, schematic);

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
