/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.core.Position;

public class BuildingSlotBlock extends BuildingSlot implements Comparable<BuildingSlotBlock> {

	public int x, y, z;
	public Schematic schematic;

	public enum Mode {
		ClearIfInvalid, Build
	};

	public Mode mode = Mode.Build;

	public Schematic getSchematic () {
		if (schematic == null) {
			return new SchematicMask(false);
		} else {
			return schematic;
		}
	}

	@Override
	public void writeToWorld(IBuilderContext context) {
		if (mode == Mode.ClearIfInvalid) {
			if (!getSchematic().isValid(context, x, y, z)) {
				context.world().setBlockToAir(x, y, z);
			}
		} else {
			try {
				getSchematic().writeToWorld(context, x, y, z);

				// Once the schematic has been written, we're going to issue
				// calls
				// to various functions, in particular updating the tile entity.
				// If these calls issue problems, in order to avoid corrupting
				// the world, we're logging the problem and setting the block to
				// air.

				TileEntity e = context.world().getTileEntity(x, y, z);

				if (e != null) {
					e.updateEntity();
				}
			} catch (Throwable t) {
				t.printStackTrace();
				context.world().setBlockToAir(x, y, z);
			}
		}
	}

	@Override
	public void postProcessing (IBuilderContext context) {
		getSchematic().postProcessing(context, x, y, z);
	}

	@Override
	public LinkedList<ItemStack> getRequirements (IBuilderContext context) {
		if (mode == Mode.ClearIfInvalid) {
			return new LinkedList<ItemStack>();
		} else {
			return getSchematic().getRequirements(context);
		}
	}

	@Override
	public int compareTo(BuildingSlotBlock o) {
		if (o.schematic instanceof Comparable && schematic instanceof Comparable ) {
			Comparable comp1 = (Comparable) schematic;
			Comparable comp2 = (Comparable) o.schematic;

			int cmp = comp1.compareTo(comp2);

			if (cmp != 0) {
				return cmp;
			}
		}

		if (y < o.y) {
			return -1;
		} else if (y > o.y) {
			return 1;
		} else if (x < o.x) {
			return -1;
		} else if (x > o.x) {
			return 1;
		} else if (z < o.z) {
			return -1;
		} else if (z > o.z) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public Position getDestination () {
		return new Position (x + 0.5, y + 0.5, z + 0.5);
	}
}
