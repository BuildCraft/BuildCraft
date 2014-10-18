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
import java.util.LinkedList;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.api.blueprints.ITileBuilder;
import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.api.core.NetworkData;
import buildcraft.core.IBoxProvider;
import buildcraft.core.LaserData;
import buildcraft.core.RFBattery;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import buildcraft.core.network.RPCSide;

public abstract class TileAbstractBuilder extends TileBuildCraft implements ITileBuilder, IInventory, IBoxProvider,
		IBuildingItemsProvider {

	/**
	 * Computes the maximum amount of energy required to build a full chest,
	 * plus a safeguard. That's a nice way to evaluate maximum amount of energy
	 * that need to be in a builder.
	 */
	private static final int FULL_CHEST_ENERGY = 9 * 3 * 64 * SchematicRegistry.BUILD_ENERGY + 10000;

	@NetworkData
	public LinkedList<LaserData> pathLasers = new LinkedList<LaserData> ();

	public ArrayList<BuildingItem> buildersInAction = new ArrayList<BuildingItem>();

	private int rfPrev = 0;
	private int rfUnchangedCycles = 0;

	public TileAbstractBuilder() {
		super();
		this.setBattery(new RFBattery(FULL_CHEST_ENERGY, 1000, 0));
	}
	@Override
	public void initialize () {
		super.initialize();

		if (worldObj.isRemote) {
			RPCHandler.rpcServer(this, "uploadBuildersInAction");
		}
	}

	@RPC (RPCSide.SERVER)
	private void uploadBuildersInAction (RPCMessageInfo info) {
		for (BuildingItem i : buildersInAction) {
			RPCHandler.rpcPlayer(info.sender, this, "launchItem", i);
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		RFBattery battery = this.getBattery();

		if (rfPrev != battery.getEnergyStored()) {
			rfPrev = battery.getEnergyStored();
			rfUnchangedCycles = 0;
		}

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

		if (rfPrev != battery.getEnergyStored()) {
			rfPrev = battery.getEnergyStored();
			rfUnchangedCycles = 0;
		}

		rfUnchangedCycles++;

		/**
		 * After 100 cycles with no consumption or additional power, start to
		 * slowly to decrease the amount of power available in the builder.
		 */
		if (rfUnchangedCycles > 100) {
			battery.useEnergy(0, 1000, false);

			rfPrev = battery.getEnergyStored();
		}
	}

	@Override
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

	@Override
	public void addAndLaunchBuildingItem(BuildingItem item) {
		buildersInAction.add(item);
		RPCHandler.rpcBroadcastWorldPlayers(worldObj, this, "launchItem", item);
	}

	public final int energyAvailable() {
		return getBattery().getEnergyStored();
	}

	public final boolean consumeEnergy(int quantity) {
		return getBattery().useEnergy(quantity, quantity, false) > 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		rfPrev = getBattery().getEnergyStored();
		rfUnchangedCycles = 0;
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return Double.MAX_VALUE;
	}
}
