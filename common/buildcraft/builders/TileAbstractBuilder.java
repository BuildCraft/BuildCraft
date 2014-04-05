/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.inventory.IInventory;
import buildcraft.api.mj.MjBattery;
import buildcraft.core.LaserData;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;

public abstract class TileAbstractBuilder extends TileBuildCraft implements IInventory {

	public static double BREAK_ENERGY = 10;
	public static double BUILD_ENERGY = 20;

	@MjBattery(maxReceivedPerCycle = 100, maxCapacity = 1000, minimumConsumption = 1)
	protected double mjStored = 0;

	@NetworkData
	public LinkedList<LaserData> pathLasers = new LinkedList<LaserData> ();

	public ArrayList <BuildingItem> buildersInAction = new ArrayList<BuildingItem>();

	@Override
	public void updateEntity() {
		super.updateEntity();

		BuildingItem toRemove = null;

		for (BuildingItem i : buildersInAction) {
			i.update();

			if (i.isDone) {
				toRemove = i;
			}
		}

		if (toRemove != null) {
			buildersInAction.remove(toRemove);
		}
	}

	public ArrayList<BuildingItem> getBuilders() {
		return buildersInAction;
	}

	public LinkedList<LaserData> getPathLaser() {
		return pathLasers;
	}

	@RPC (RPCSide.CLIENT)
	public void launchItem (BuildingItem item) {
		buildersInAction.add(item);
	}

	public void addBuildingItem(BuildingItem item) {
		buildersInAction.add(item);
		RPCHandler.rpcBroadcastPlayers(this, "launchItem", item);
	}

	public abstract boolean isBuildingMaterialSlot(int i);

	public final double energyAvailable() {
		return mjStored;
	}

	public final void consumeEnergy(double quantity) {
		mjStored -= quantity;
	}


}
