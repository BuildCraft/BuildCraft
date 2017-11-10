/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.boards;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSettings;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.builders.TileConstructionMarker;
import buildcraft.core.builders.BuildingItem;
import buildcraft.core.builders.BuildingSlot;
import buildcraft.core.lib.inventory.filters.ArrayStackFilter;
import buildcraft.robotics.ai.AIRobotDisposeItems;
import buildcraft.robotics.ai.AIRobotGotoBlock;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraft.robotics.ai.AIRobotRecharge;

public class BoardRobotBuilder extends RedstoneBoardRobot {

	private static final int MAX_RANGE_SQ = 3 * 64 * 64;

	private TileConstructionMarker markerToBuild;
	private BuildingSlot currentBuildingSlot;
	private LinkedList<ItemStack> requirementsToLookFor;
	private int launchingDelay = 0;

	public BoardRobotBuilder(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BCBoardNBT.REGISTRY.get("builder");
	}

	@Override
	public void update() {
		if (launchingDelay > 0) {
			launchingDelay--;
			return;
		}

		if (markerToBuild == null) {
			markerToBuild = findClosestMarker();

			if (markerToBuild == null) {
				if (robot.containsItems()) {
					startDelegateAI(new AIRobotDisposeItems(robot));
				} else {
					startDelegateAI(new AIRobotGotoSleep(robot));
				}
				return;
			}
		}

		if (!markerToBuild.needsToBuild()) {
			markerToBuild = null;
			currentBuildingSlot = null;
			return;
		}

		if (currentBuildingSlot == null) {
			currentBuildingSlot = markerToBuild.bluePrintBuilder.reserveNextSlot(robot.worldObj);

			if (currentBuildingSlot == null) {
				// No slots available yet
				launchingDelay = 40;
				return;
			}

		}

		if (requirementsToLookFor == null) {
			if (robot.containsItems()) {
				startDelegateAI(new AIRobotDisposeItems(robot));
			}

			if (robot.worldObj.getWorldInfo().getGameType() != WorldSettings.GameType.CREATIVE) {
				requirementsToLookFor = currentBuildingSlot.getRequirements(markerToBuild
						.getContext());
			} else {
				requirementsToLookFor = new LinkedList<ItemStack>();
			}

			if (requirementsToLookFor == null) {
				launchingDelay = 40;
				return;
			}

			if (requirementsToLookFor.size() > 4) {
				currentBuildingSlot.built = true;
				currentBuildingSlot = null;
				requirementsToLookFor = null;
				return;
			}
		}

		if (requirementsToLookFor.size() > 0) {
			startDelegateAI(new AIRobotGotoStationAndLoad(robot, new ArrayStackFilter(
					requirementsToLookFor.getFirst()), requirementsToLookFor.getFirst().stackSize));
			return;
		}

		if (requirementsToLookFor.size() == 0) {
			if (currentBuildingSlot.stackConsumed == null) {
				// Once all the element are in, if not already, use them to
				// prepare the slot.
				markerToBuild.bluePrintBuilder.useRequirements(robot, currentBuildingSlot);
			}

			if (!hasEnoughEnergy()) {
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
		if (ai instanceof AIRobotGotoStationAndLoad) {
			if (ai.success()) {
				requirementsToLookFor.removeFirst();
			} else {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotGotoBlock) {
			if (markerToBuild == null || markerToBuild.bluePrintBuilder == null) {
				// defensive code, in case of a wrong load from NBT
				return;
			}

			if (!hasEnoughEnergy()) {
				startDelegateAI(new AIRobotRecharge(robot));
				return;
			}

			robot.getBattery().extractEnergy(currentBuildingSlot.getEnergyRequirement(), false);
			launchingDelay = currentBuildingSlot.getStacksToDisplay().size()
					* BuildingItem.ITEMS_SPACE;
			markerToBuild.bluePrintBuilder.buildSlot(robot.worldObj, markerToBuild,
					currentBuildingSlot, robot.posX + 0.125F, robot.posY + 0.125F,
					robot.posZ + 0.125F);
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

	private TileConstructionMarker findClosestMarker() {
		double minDistance = Double.MAX_VALUE;
		TileConstructionMarker minMarker = null;

		IZone zone = robot.getZoneToWork();

		for (TileConstructionMarker marker : TileConstructionMarker.currentMarkers) {
			if (marker.getWorldObj() != robot.worldObj) {
				continue;
			}
			if (!marker.needsToBuild()) {
				continue;
			}
			if (zone != null && !zone.contains(marker.xCoord, marker.yCoord, marker.zCoord)) {
				continue;
			}

			double dx = robot.posX - marker.xCoord;
			double dy = robot.posY - marker.yCoord;
			double dz = robot.posZ - marker.zCoord;
			double distance = dx * dx + dy * dy + dz * dz;

			if (distance < minDistance) {
				minMarker = marker;
				minDistance = distance;
			}
		}

		if (minMarker != null && minDistance < MAX_RANGE_SQ) {
			return minMarker;
		} else {
			return null;
		}
	}

	private boolean hasEnoughEnergy() {
		return robot.getEnergy() - currentBuildingSlot.getEnergyRequirement() > EntityRobotBase.SAFETY_ENERGY;
	}

}
