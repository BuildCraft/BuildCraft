/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.core.lib.utils.IBlockFilter;

public class AIRobotSearchAndGotoBlock extends AIRobot {

	private BlockIndex blockFound;

	private IBlockFilter filter;
	private boolean random;
	private double maxDistanceToEnd;

	public AIRobotSearchAndGotoBlock(EntityRobotBase iRobot) {
		super(iRobot);

		blockFound = null;

		random = false;
		filter = null;
	}

	public AIRobotSearchAndGotoBlock(EntityRobotBase iRobot, boolean iRandom,
									 IBlockFilter iPathFound) {
		this(iRobot, iRandom, iPathFound, 0);
	}

	public AIRobotSearchAndGotoBlock(EntityRobotBase iRobot, boolean iRandom,
									 IBlockFilter iPathFound, double iMaxDistanceToEnd) {
		this(iRobot);

		random = iRandom;
		filter = iPathFound;
		maxDistanceToEnd = iMaxDistanceToEnd;
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotSearchBlock(robot, random, filter, maxDistanceToEnd));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchBlock) {
			if (ai.success()) {
				AIRobotSearchBlock searchAI = (AIRobotSearchBlock) ai;
				if (searchAI.takeResource()) {
					blockFound = searchAI.blockFound;
					startDelegateAI(new AIRobotGotoBlock(robot, searchAI.path));
				} else {
					terminate();
				}
			} else {
				terminate();
			}
		} else if (ai instanceof AIRobotGotoBlock) {
			if (!ai.success()) {
				releaseBlockFound();
			}
			terminate();
		}
	}

	@Override
	public boolean success() {
		return blockFound != null;
	}

	private void releaseBlockFound() {
		if (blockFound != null) {
			robot.getRegistry().release(new ResourceIdBlock(blockFound));
			blockFound = null;
		}
	}

	public BlockIndex getBlockFound() {
		return blockFound;
	}

	@Override
	public boolean canLoadFromNBT() {
		return true;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (blockFound != null) {
			NBTTagCompound sub = new NBTTagCompound();
			blockFound.writeTo(sub);
			nbt.setTag("indexStored", sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("indexStored")) {
			blockFound = new BlockIndex(nbt.getCompoundTag("indexStored"));
		}
	}
}
