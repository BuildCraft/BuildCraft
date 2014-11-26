/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.builders.TileConstructionMarker;
import buildcraft.core.builders.BuildingItem;
import buildcraft.core.builders.BuildingSlot;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.robots.AIRobotGotoBlock;
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotGotoStationToLoad;
import buildcraft.core.robots.AIRobotLoad;
import buildcraft.core.robots.AIRobotRecharge;

public class BoardRobotBuilder extends RedstoneBoardRobot {

	private TileConstructionMarker markerToBuild;
	private BuildingSlot currentBuildingSlot;
	private LinkedList<ItemStack> requirementsToLookFor;
	private int launchingDelay = 0;

	public BoardRobotBuilder(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotBuilderNBT.instance;
	}

	@Override
	public void update() {
		if (launchingDelay > 0) {
			launchingDelay--;
			return;
		}

		if (markerToBuild == null) {
			double minDistance = Double.MAX_VALUE;

			for (TileConstructionMarker marker : TileConstructionMarker.currentMarkers) {
				if (marker.getWorld() == robot.worldObj && marker.needsToBuild()) {
					double dx = robot.posX - marker.xCoord;
					double dy = robot.posY - marker.yCoord;
					double dz = robot.posZ - marker.zCoord;
					double distance = dx * dx + dy * dy + dz * dz;

					if (distance < minDistance) {
						markerToBuild = marker;
						minDistance = distance;
					}
				}
			}

			if (markerToBuild == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
				return;
			}
		}

		if (markerToBuild == null || !markerToBuild.needsToBuild()) {
			markerToBuild = null;
			startDelegateAI(new AIRobot(robot));
			return;
		}

		if (currentBuildingSlot == null) {
			currentBuildingSlot = markerToBuild.bluePrintBuilder.reserveNextSlot(robot.worldObj);

			if (currentBuildingSlot != null) {
				// The above may return null even if not done, if it's scanning
				// for available blocks.
				requirementsToLookFor = currentBuildingSlot.getRequirements(markerToBuild.getContext());
			}

			// TODO: what if there's more requirements that this robot can
			// handle e.g. not enough free spots? If there's more than X slots
			// found that can't be built, go to sleep.
		}

		if (requirementsToLookFor != null && requirementsToLookFor.size() > 0) {
			startDelegateAI(new AIRobotGotoStationToLoad(robot,
					new ArrayStackFilter(requirementsToLookFor.getFirst()),
					robot.getZoneToWork()));
		}

		if (currentBuildingSlot != null && requirementsToLookFor != null && requirementsToLookFor.size() == 0) {
			if (currentBuildingSlot.stackConsumed == null) {
				// Once all the element are in, if not already, use them to
				// prepare the slot.
				markerToBuild.bluePrintBuilder.useRequirements(robot, currentBuildingSlot);
			}

			if (robot.getEnergy() - currentBuildingSlot.getEnergyRequirement() < EntityRobotBase.SAFETY_ENERGY) {
				startDelegateAI(new AIRobotRecharge(robot));
			} else {
				startDelegateAI(new AIRobotGotoBlock(robot,
					(int) currentBuildingSlot.getDestination().x,
					(int) currentBuildingSlot.getDestination().y,
					(int) currentBuildingSlot.getDestination().z,
					8));
			}
			// TODO: take into account cases where the robot can't reach the
			// destination - go to work on another block
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoad) {
			if (ai.success()) {
				startDelegateAI(new AIRobotLoad(robot, new ArrayStackFilter(requirementsToLookFor.getFirst()),
						requirementsToLookFor.getFirst().stackSize));
			} else {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotLoad) {
			// TODO: check that we get the proper items in
			requirementsToLookFor.removeFirst();
		} else if (ai instanceof AIRobotGotoBlock) {
			if (markerToBuild == null || markerToBuild.bluePrintBuilder == null) {
				// defensive code, in case of a wrong load from NBT
				return;
			}

			if (robot.getEnergy() - currentBuildingSlot.getEnergyRequirement() < EntityRobotBase.SAFETY_ENERGY) {
				startDelegateAI(new AIRobotRecharge(robot));
				return;
			}

			robot.getBattery().extractEnergy(currentBuildingSlot.getEnergyRequirement(), false);
			launchingDelay = currentBuildingSlot.getStacksToDisplay().size() * BuildingItem.ITEMS_SPACE;
			markerToBuild.bluePrintBuilder.buildSlot
					(robot.worldObj, markerToBuild, currentBuildingSlot,
							robot.posX + 0.125F, robot.posY + 0.125F, robot.posZ + 0.125F);
			currentBuildingSlot = null;
			requirementsToLookFor = null;
		}
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		nbt.setInteger("launchingDelay", launchingDelay);
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		launchingDelay = nbt.getInteger("launchingDelay");
	}
}
