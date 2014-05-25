/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.tests.testcase;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.api.core.NetworkData;

public class SequenceActionCheckBlockMeta extends SequenceAction {

	@NetworkData
	String blockName;

	@NetworkData
	int meta;

	@NetworkData
	int x, y, z;

	public SequenceActionCheckBlockMeta() {

	}

	public SequenceActionCheckBlockMeta(World iWorld, int ix, int iy, int iz) {
		x = ix;
		y = iy;
		z = iz;
		world = iWorld;
		date = world.getTotalWorldTime();

		blockName = Block.blockRegistry.getNameForObject(world.getBlock(x, y, z));
		meta = world.getBlockMetadata(x, y, z);
	}

	@Override
	public void execute() {
		String worldBlockName = Block.blockRegistry.getNameForObject(world.getBlock(x, y, z));
		int worldMeta = world.getBlockMetadata(x, y, z);

		if (!worldBlockName.equals(blockName)) {
			System.out.println("[TEST " + date + "] [ERROR] " + x + ", " + y + ", " + z + " block " + blockName
					+ " expected, " + worldBlockName + " found.");
		} else if (meta != worldMeta) {
			System.out.println("[TEST " + date + "] [ERROR] " + x + ", " + y + ", " + z + " meta " + meta
					+ " expected, " + worldMeta + " found.");
		} else {
			System.out.println("[TEST " + date + "] [OK] " + x + ", " + y + ", " + z + " is {" + blockName + ", "
					+ meta
					+ "}");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
		nbt.setInteger("meta", meta);
		nbt.setString("block", blockName);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		x = nbt.getInteger("x");
		y = nbt.getInteger("y");
		z = nbt.getInteger("z");
		meta = nbt.getInteger("meta");
		blockName = nbt.getString("block");
	}
}