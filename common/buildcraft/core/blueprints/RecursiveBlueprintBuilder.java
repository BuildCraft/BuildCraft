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

import buildcraft.core.Box;

public class RecursiveBlueprintBuilder {

	private boolean returnedThis = false;
	private BlueprintBase blueprint;
	private RecursiveBlueprintBuilder current;
	private int nextSubBlueprint = 0;
	private ArrayList<NBTTagCompound> subBlueprints = new ArrayList<NBTTagCompound>();
	private int x, y, z;
	private ForgeDirection dir;
	private World world;
	private Box box = new Box();

	public RecursiveBlueprintBuilder(BlueprintBase iBlueprint, World iWorld, int iX, int iY, int iZ,
			ForgeDirection iDir) {
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

			BptBuilderBase builder;

			if (blueprint instanceof Blueprint) {
				builder = new BptBuilderBlueprint((Blueprint) blueprint, world, x, y, z);
			} else if (blueprint instanceof Template) {
				builder = new BptBuilderTemplate(blueprint, world, x, y, z);
			} else {
				return null;
			}

			box.initialize(builder);

			return builder;
		}

		// Free memory associated with this blueprint
		blueprint = null;

		if (current != null) {
			BptBuilderBase builder = current.nextBuilder();

			if (builder != null) {
				return builder;
			}
		}

		if (nextSubBlueprint >= subBlueprints.size()) {
			return null;
		}

		NBTTagCompound nbt = subBlueprints.get(nextSubBlueprint);
		BlueprintBase bpt = BlueprintBase.loadBluePrint(nbt.getCompoundTag("bpt"));

		int nx = box.xMin + nbt.getInteger("x");
		int ny = box.yMin + nbt.getInteger("y");
		int nz = box.zMin + nbt.getInteger("z");

		ForgeDirection nbtDir = ForgeDirection.values()[nbt.getByte("dir")];

		current = new RecursiveBlueprintBuilder(bpt, world, nx, ny, nz, nbtDir);
		nextSubBlueprint++;

		return current.nextBuilder();
	}
}
