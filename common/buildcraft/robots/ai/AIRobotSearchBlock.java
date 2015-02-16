/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.ai;

import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.utils.IBlockFilter;
import buildcraft.core.utils.concurrency.IterableAlgorithmRunner;
import buildcraft.core.utils.concurrency.PathFindingSearch;

public class AIRobotSearchBlock extends AIRobot {

	public BlockIndex blockFound;
	public LinkedList<BlockIndex> path;
	private PathFindingSearch blockScanner = null;
	private IterableAlgorithmRunner blockScannerJob;
	private IBlockFilter pathFound;

	public AIRobotSearchBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotSearchBlock(EntityRobotBase iRobot, IBlockFilter iPathFound) {
		super(iRobot);

		pathFound = iPathFound;
	}

	@Override
	public void start() {
		blockScanner = new PathFindingSearch(robot.worldObj, new BlockIndex(robot), pathFound, 96, robot.getZoneToWork());
		blockScannerJob = new IterableAlgorithmRunner(blockScanner, 40000);
		blockScannerJob.start();
	}

	@Override
	public void update() {
		if (blockScannerJob == null) {
			// This is probably due to a load from NBT. Abort the ai in
			// that case, since there's no filter to analyze either.
			abort();
			return;
		}

		if (blockScannerJob.isDone()) {
			path = blockScanner.getResult();

			if (path != null && path.size() > 0) {
				blockFound = path.removeLast();
			} else {
				path = null;
			}

			terminate();
		}
	}

	@Override
	public void end() {
		if (blockScannerJob != null) {
			blockScannerJob.terminate();
		}
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (blockFound != null) {
			NBTTagCompound sub = new NBTTagCompound();
			blockFound.writeTo(sub);
			nbt.setTag("blockFound", sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("blockFound")) {
			blockFound = new BlockIndex(nbt.getCompoundTag("blockFound"));
		}
	}

	public void unreserve() {
		blockScanner.unreserve(blockFound);
	}

	@Override
	public int getEnergyCost() {
		return 2;
	}

}
