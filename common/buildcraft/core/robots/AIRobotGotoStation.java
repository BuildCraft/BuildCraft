/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.util.EnumFacing;

import net.minecraft.util.BlockPos;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.core.utils.Utils;

public class AIRobotGotoStation extends AIRobot {

	private BlockPos stationIndex;
	private EnumFacing stationSide;
	private boolean docked = false;

	public AIRobotGotoStation(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStation(EntityRobotBase iRobot, IDockingStation station) {
		super(iRobot);

		stationIndex = station.pos();
		stationSide = station.side();
	}

	@Override
	public void start() {
		DockingStation station = (DockingStation)
				robot.getRegistry().getStation(stationIndex, stationSide);

		if (station == null || station == robot.getDockingStation()) {
			terminate();
		} else {
			if (station.take(robot)) {
				startDelegateAI(new AIRobotGotoBlock(robot, station.pos().offset(station.side())));
			} else {
				terminate();
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		DockingStation station = (DockingStation)
				robot.getRegistry().getStation(stationIndex, stationSide);

		if (station == null) {
			terminate();
		} else if (ai instanceof AIRobotGotoBlock) {
			startDelegateAI(new AIRobotStraightMoveTo(robot,
					stationIndex.getX() + 0.5F + stationSide.getFrontOffsetX() * 0.5F,
					stationIndex.getY() + 0.5F + stationSide.getFrontOffsetY() * 0.5F,
					stationIndex.getZ() + 0.5F + stationSide.getFrontOffsetZ() * 0.5F));
		} else {
			docked = true;
			robot.dock(station);
			terminate();
		}
	}

	@Override
	public boolean success() {
		return docked;
	}

	@Override
	public boolean canLoadFromNBT() {
		return true;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		NBTTagCompound indexNBT = new NBTTagCompound();
		Utils.writeBlockPos(indexNBT, stationIndex);
		nbt.setTag("stationIndex", indexNBT);
		nbt.setByte("stationSide", (byte) stationSide.ordinal());
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		stationIndex = Utils.readBlockPos(nbt.getCompoundTag("stationIndex"));
		stationSide = EnumFacing.values()[nbt.getByte("stationSide")];
	}
}
