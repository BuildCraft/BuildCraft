/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.robotics.DockingStation;

public class AIRobotGotoStation extends AIRobot {

	private BlockIndex stationIndex;
	private ForgeDirection stationSide;
	private boolean docked = false;

	public AIRobotGotoStation(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStation(EntityRobotBase iRobot, IDockingStation station) {
		super(iRobot);

		stationIndex = station.index();
		stationSide = station.side();
	}

	@Override
	public void start() {
		DockingStation station = (DockingStation)
				robot.getRegistry().getStation(stationIndex.x, stationIndex.y, stationIndex.z,
				stationSide);

		if (station == null || station == robot.getDockingStation()) {
			terminate();
		} else {
			if (station.take(robot)) {
				startDelegateAI(new AIRobotGotoBlock(robot,
						station.x() + station.side().offsetX,
						station.y() + station.side().offsetY,
						station.z() + station.side().offsetZ));
			} else {
				terminate();
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		DockingStation station = (DockingStation)
				robot.getRegistry().getStation(stationIndex.x, stationIndex.y, stationIndex.z,
						stationSide);

		if (station == null) {
			terminate();
		} else if (ai instanceof AIRobotGotoBlock) {
			startDelegateAI(new AIRobotStraightMoveTo(robot,
					stationIndex.x + 0.5F + stationSide.offsetX * 0.5F,
					stationIndex.y + 0.5F + stationSide.offsetY * 0.5F,
					stationIndex.z + 0.5F + stationSide.offsetZ * 0.5F));
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
		stationIndex.writeTo(indexNBT);
		nbt.setTag("stationIndex", indexNBT);
		nbt.setByte("stationSide", (byte) stationSide.ordinal());
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		stationIndex = new BlockIndex(nbt.getCompoundTag("stationIndex"));
		stationSide = ForgeDirection.values()[nbt.getByte("stationSide")];
	}
}
