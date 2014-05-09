/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gates;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.GateExpansionController;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.MjAPI;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.triggers.ActionEnergyPulsar;
import buildcraft.transport.triggers.ActionSingleEnergyPulse;

public final class GateExpansionPulsar extends GateExpansionBuildcraft implements IGateExpansion {

	public static GateExpansionPulsar INSTANCE = new GateExpansionPulsar();

	private GateExpansionPulsar() {
		super("pulsar");
	}

	@Override
	public GateExpansionController makeController(TileEntity pipeTile) {
		return new GateExpansionControllerPulsar(pipeTile);
	}

	private class GateExpansionControllerPulsar extends GateExpansionController {

		private static final int PULSE_PERIOD = 10;
		private boolean isActive;
		private boolean singlePulse;
		private boolean hasPulsed;
		private int pulseCount;
		private int tick;

		public GateExpansionControllerPulsar(TileEntity pipeTile) {
			super(GateExpansionPulsar.this, pipeTile);

			// by default, initialize tick so that not all gates created at
			// one single moment would do the work at the same time. This
			// spreads a bit work load. Note, this is not a problem for
			// existing gates since tick is stored in NBT
			tick = (int) Math.random() * PULSE_PERIOD;
		}

		@Override
		public void startResolution() {
			if (isActive()) {
				disablePulse();
			}
		}

		@Override
		public boolean resolveAction(IAction action, int count) {
			if (action instanceof ActionEnergyPulsar) {
				enablePulse(count);
				return true;
			} else if (action instanceof ActionSingleEnergyPulse) {
				enableSinglePulse(count);
				return true;
			}
			return false;
		}

		@Override
		public void addActions(List<IAction> list) {
			super.addActions(list);
			list.add(BuildCraftTransport.actionEnergyPulser);
			list.add(BuildCraftTransport.actionSingleEnergyPulse);
		}

		@Override
		public void tick() {
			if (!isActive && hasPulsed) {
				hasPulsed = false;
			}

			if (tick++ % PULSE_PERIOD != 0) {
				// only do the treatement once every period
				return;
			}

			if (!isActive) {
				((TileGenericPipe) pipeTile).pipe.gate.setPulsing(false);
				return;
			}

			IBatteryObject battery = MjAPI.getMjBattery(pipeTile);

			if (battery != null && (!singlePulse || !hasPulsed)) {
				((TileGenericPipe) pipeTile).pipe.gate.setPulsing(true);
				battery.addEnergy(Math.min(1 << (pulseCount - 1), 64) * 1.01f);
				hasPulsed = true;
			} else {
				((TileGenericPipe) pipeTile).pipe.gate.setPulsing(true);
			}
		}

		private void enableSinglePulse(int count) {
			singlePulse = true;
			isActive = true;
			pulseCount = count;
		}

		private void enablePulse(int count) {
			isActive = true;
			singlePulse = false;
			pulseCount = count;
		}

		private void disablePulse() {
			if (!isActive) {
				hasPulsed = false;
			}
			isActive = false;
			pulseCount = 0;
		}

		@Override
		public boolean isActive() {
			return isActive;
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setBoolean("singlePulse", singlePulse);
			nbt.setBoolean("isActive", isActive);
			nbt.setBoolean("hasPulsed", hasPulsed);
			nbt.setInteger("pulseCount", pulseCount);
			nbt.setInteger("tick", tick);
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			isActive = nbt.getBoolean("isActive");
			singlePulse = nbt.getBoolean("singlePulse");
			hasPulsed = nbt.getBoolean("hasPulsed");
			pulseCount = nbt.getInteger("pulseCount");
			tick = nbt.getInteger("tick");
		}
	}
}
