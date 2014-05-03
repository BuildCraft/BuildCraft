/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import java.util.Map;
import java.util.TreeMap;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.MjBattery;
import buildcraft.core.BlockIndex;
import buildcraft.core.LaserData;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import buildcraft.core.network.RPCSide;

public class TileEnergyEmitter extends TileBuildCraft {

	@MjBattery (maxCapacity = 1024, maxReceivedPerCycle = 1204, minimumConsumption = 0)
	public double mjStored;

	public Map<BlockIndex, Target> targets = new TreeMap<BlockIndex, Target>();

	public float mjAcc = 0;
	public int accumulated = 0;

	private SafeTimeTracker syncMJ = new SafeTimeTracker(20, 5);
	private SafeTimeTracker scanTracker = new SafeTimeTracker(100, 10);

	public static class Target {
		public LaserData data = new LaserData();
		TileEnergyReceiver receiver;
	}

	public TileEnergyEmitter () {

	}

	@Override
	public void initialize () {
		super.initialize();

		if (worldObj.isRemote) {
			RPCHandler.rpcServer(this, "requestLasers");
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			for (Target t : targets.values()) {
				if (t.data.isVisible) {
					t.data.update();
					t.data.wavePosition += 0.2F;

					if (t.data.wavePosition > t.data.renderSize) {
						t.data.wavePosition = 0;
						t.data.waveSize = (float) (mjStored / targets.size() / 10F);

						if (t.data.waveSize > 1) {
							t.data.waveSize = 1F;
						}
					}

					t.data.iterateTexture();
				}
			}

			return;
		}

		if (scanTracker.markTimeIfDelay(worldObj)) {
			for (TileEnergyReceiver receiver : TileEnergyReceiver.knownReceivers) {
				float dx = xCoord - receiver.xCoord;
				float dy = yCoord - receiver.yCoord;
				float dz = zCoord - receiver.zCoord;

				if (dx * dx + dy * dy + dz * dz < 100 * 100) {
					BlockIndex index = new BlockIndex(receiver.xCoord, receiver.yCoord, receiver.zCoord);

					if (!targets.containsKey(index)) {
						addLaser(receiver.xCoord, receiver.yCoord,
								receiver.zCoord);

						RPCHandler.rpcBroadcastPlayers(this, "addLaser",
								receiver.xCoord, receiver.yCoord,
								receiver.zCoord);

						targets.get(index).receiver = receiver;
					}
				}
			}
		}

		// synchronize regularly with the client an average of the energy in
		// the emitter

		mjAcc += mjStored;
		accumulated++;

		if (syncMJ.markTimeIfDelay(worldObj)) {
			RPCHandler.rpcBroadcastPlayers(this, "synchronizeMJ", mjAcc
					/ accumulated);
			mjAcc = 0;
			accumulated = 0;
		}

		if (mjStored == 0) {
			for (Target t : targets.values()) {
				if (t.data.isVisible) {
					t.data.isVisible = false;
					RPCHandler.rpcBroadcastPlayers(this, "disableLaser",
							t.receiver.xCoord, t.receiver.yCoord,
							t.receiver.zCoord);
				}
			}
		} else {
			double perTargetEnergy = 10;

			if (mjStored > targets.size() * 10) {
				mjStored -= targets.size() * 10;
			} else {
				perTargetEnergy = mjStored / targets.size();
				mjStored = 0;
			}

			for (Target t : targets.values()) {
				if (!t.data.isVisible) {
					t.data.isVisible = true;
					RPCHandler.rpcBroadcastPlayers(this, "enableLaser",
							t.receiver.xCoord, t.receiver.yCoord,
							t.receiver.zCoord);
				}
			}

			for (Target t : targets.values()) {
				t.receiver.energyStored += perTargetEnergy;
			}
		}
	}

	@RPC (RPCSide.CLIENT)
	public void synchronizeMJ (float val) {
		mjStored = val;
	}

	@RPC (RPCSide.CLIENT)
	public void addLaser (int x, int y, int z) {
		BlockIndex index = new BlockIndex(x, y, z);

		if (!targets.containsKey(index)) {
			Target t = new Target();

			t.data.head.x = xCoord + 0.5F;
			t.data.head.y = yCoord + 0.5F;
			t.data.head.z = zCoord + 0.5F;

			t.data.tail.x = x + 0.5F;
			t.data.tail.y = y + 0.5F;
			t.data.tail.z = z + 0.5F;

			targets.put(index, t);
		}
	}

	@RPC (RPCSide.CLIENT)
	public void enableLaser (int x, int y, int z) {
		BlockIndex index = new BlockIndex(x, y, z);

		if (targets.containsKey(index)) {
			targets.get(index).data.isVisible = true;
		}
	}

	@RPC (RPCSide.CLIENT)
	public void disableLaser (int x, int y, int z) {
		BlockIndex index = new BlockIndex(x, y, z);

		if (targets.containsKey(index)) {
			targets.get(index).data.isVisible = false;
		}
	}

	@RPC (RPCSide.SERVER)
	public void requestLasers (RPCMessageInfo info) {
		for (BlockIndex b : targets.keySet()) {
			RPCHandler.rpcPlayer(this, "addLaser", info.sender, b.x, b.y, b.z);
		}
	}

	@Override
	public World getWorld() {
		return worldObj;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		double xMin = xCoord;
		double yMin = yCoord;
		double zMin = zCoord;
		double xMax = xCoord + 1.0;
		double yMax = yCoord + 1.0;
		double zMax = zCoord + 1.0;

		for (Target t : targets.values()) {
			if (t.data.tail.x < xMin) {
				xMin = t.data.tail.x;
			}

			if (t.data.tail.y < yMin) {
				yMin = t.data.tail.y;
			}

			if (t.data.tail.z < zMin) {
				zMin = t.data.tail.z;
			}

			if (t.data.tail.x > xMax) {
				xMax = t.data.tail.x;
			}

			if (t.data.tail.y > yMax) {
				yMax = t.data.tail.y;
			}

			if (t.data.tail.z > zMax) {
				zMax = t.data.tail.z;
			}
		}

		return AxisAlignedBB.getBoundingBox(xMin, yMin, zMin, xMax, yMax, zMax);
	}
}
