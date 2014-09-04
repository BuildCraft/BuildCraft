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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

public class RecursiveBlueprintBuilder {

	private boolean returnedThis = false;
	private BlueprintBase blueprint;
	private RecursiveBlueprintBuilder current;
	private int nextSubBlueprint = 0;
	private ArrayList<NBTTagCompound> subBlueprints = new ArrayList<NBTTagCompound>();
	private int x, y, z;
	private ForgeDirection dir;
	private World world;

	public RecursiveBlueprintBuilder(BlueprintBase iBlueprint, World iWorld, int iX, int iY, int iZ, ForgeDirection iDir) {
		blueprint = iBlueprint;
		subBlueprints = iBlueprint.subBlueprintsNBT;
		world = iWorld;
		x = iX;
		y = iY;
		z = iZ;
		dir = iDir;
	}

	public BptBuilderBase nextBuilder() {
		if (!returnedThis) {
			blueprint.adjustToWorld(world, x, y, x, dir);

			returnedThis = true;

			if (blueprint instanceof Blueprint) {
				return new BptBuilderBlueprint((Blueprint) blueprint, world, x, y, z);
			} else if (blueprint instanceof Template) {
				return new BptBuilderTemplate(blueprint, world, x, y, z);
			} else {
				return null;
			}
		}

		blueprint = null;

		if (nextSubBlueprint >= subBlueprints.size()) {
			return null;
		}

		if (current != null) {
			BptBuilderBase builder = current.nextBuilder();

			if (builder != null) {
				return builder;
			}
		}

		NBTTagCompound nbt = subBlueprints.get(nextSubBlueprint);
		BlueprintBase bpt = BlueprintBase.loadBluePrint(nbt.getCompoundTag("bpt"));

		int nx = x + nbt.getInteger("x");
		int ny = y + nbt.getInteger("y");
		int nz = z + nbt.getInteger("z");

		ForgeDirection nbtDir = ForgeDirection.values()[nbt.getByte("dir")];
		ForgeDirection ndir = dir;

		if (nbtDir == ForgeDirection.EAST) {
			// Do nothing
		} else if (nbtDir == ForgeDirection.SOUTH) {
			ndir = ndir.getRotation(ForgeDirection.UP);
		} else if (nbtDir == ForgeDirection.WEST) {
			ndir = ndir.getRotation(ForgeDirection.UP);
			ndir = ndir.getRotation(ForgeDirection.UP);
		} else if (nbtDir == ForgeDirection.NORTH) {
			ndir = ndir.getRotation(ForgeDirection.UP);
			ndir = ndir.getRotation(ForgeDirection.UP);
			ndir = ndir.getRotation(ForgeDirection.UP);
		}

		current = new RecursiveBlueprintBuilder(bpt, world, nx, ny, nz, ndir);
		nextSubBlueprint++;

		return current.nextBuilder();
	}
}
