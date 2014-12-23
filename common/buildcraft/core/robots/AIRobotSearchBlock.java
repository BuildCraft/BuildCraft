/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.util.BlockPos;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.utils.PathFinding;
import buildcraft.core.utils.PathFindingJob;
import buildcraft.core.utils.Utils;

public class AIRobotSearchBlock extends AIRobot {

	public BlockPos blockFound;
	public LinkedList<BlockPos> path;
	private PathFinding blockScanner = null;
	private PathFindingJob blockScannerJob;
	private IBlockFilter pathFound;
	private int stopBefore = 0;

	public AIRobotSearchBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotSearchBlock(EntityRobotBase iRobot, IBlockFilter iPathFound) {
		super(iRobot);

		pathFound = iPathFound;
		stopBefore = 0;
	}

	@Override
	public void start() {
		blockScanner = new PathFinding(robot.worldObj, new BlockPos(robot), pathFound, 64, robot.getZoneToWork());
		blockScannerJob = new PathFindingJob(blockScanner);
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
			Utils.writeBlockPos(sub, blockFound);
			nbt.setTag("blockFound", sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("blockFound")) {
			blockFound = Utils.readBlockPos(nbt.getCompoundTag("blockFound"));
		}
	}
}
