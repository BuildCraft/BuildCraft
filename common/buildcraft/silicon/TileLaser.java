/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.silicon;

import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.core.BlockIndex;
import buildcraft.core.EntityEnergyLaser;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.proxy.CoreProxy;

public class TileLaser extends TileBuildCraft implements IPowerReceptor {

	private EntityEnergyLaser laser = null;

	private final SafeTimeTracker laserTickTracker = new SafeTimeTracker();
	private final SafeTimeTracker searchTracker = new SafeTimeTracker();
	private final SafeTimeTracker networkTracker = new SafeTimeTracker();

	private ILaserTarget laserTarget;

	public IPowerProvider powerProvider;

	private int nextNetworkUpdate = 3;
	private int nextLaserUpdate = 10;
	private int nextLaserSearch = 10;

	public TileLaser() {
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(20, 25, 25, 25, 1000);
	}

	@Override
	public void updateEntity() {

		if (!CoreProxy.proxy.isSimulating(worldObj))
			return;

		// Disable the laser and do nothing if no energy is available.
		if (powerProvider.getEnergyStored() == 0) {
			removeLaser();
			return;
		}

		// Check for available tables if none is linked to this laser.
		if (!isValidTable())
			if (canFindTable()) {
				findTable();
			}

		// If we still don't have a valid table or the existing has
		// become invalid, we disable the laser and do nothing.
		if (!isValidTable()) {
			removeLaser();
			return;
		}

		// We have a table and can work, so we create a laser if
		// necessary.
		if (laser == null) {
			createLaser();
		}

		// We have a laser and may update it
		if (laser != null && canUpdateLaser()) {
			updateLaser();
		}

		// Consume power and transfer it to the table.
		float power = powerProvider.useEnergy(0, 4, true);
		laserTarget.receiveLaserEnergy(power);

		if (laser != null) {
			laser.pushPower(power);
		}

		sendNetworkUpdate();
	}

	protected boolean canFindTable() {
		return searchTracker.markTimeIfDelay(worldObj, nextLaserSearch);
	}

	protected boolean canUpdateLaser() {
		return laserTickTracker.markTimeIfDelay(worldObj, nextLaserUpdate);
	}

	protected boolean isValidTable() {

		if (laserTarget == null || laserTarget.isInvalid() || !laserTarget.hasCurrentWork())
			return false;

		return true;
	}

	protected void findTable() {

		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

		int minX = xCoord - 5;
		int minY = yCoord - 5;
		int minZ = zCoord - 5;
		int maxX = xCoord + 5;
		int maxY = yCoord + 5;
		int maxZ = zCoord + 5;

		switch (ForgeDirection.values()[meta]) {
		case WEST:
			maxX = xCoord;
			break;
		case EAST:
			minX = xCoord;
			break;
		case DOWN:
			maxY = yCoord;
			break;
		case UP:
			minY = yCoord;
			break;
		case NORTH:
			maxZ = zCoord;
			break;
		default:
		case SOUTH:
			minZ = zCoord;
			break;
		}

		LinkedList<BlockIndex> targets = new LinkedList<BlockIndex>();

		for (int x = minX; x <= maxX; ++x) {
			for (int y = minY; y <= maxY; ++y) {
				for (int z = minZ; z <= maxZ; ++z) {

					TileEntity tile = worldObj.getBlockTileEntity(x, y, z);
					if (tile instanceof ILaserTarget) {

						ILaserTarget table = (ILaserTarget) tile;
						if (table.hasCurrentWork()) {
							targets.add(new BlockIndex(x, y, z));
						}
					}

				}
			}
		}

		if (targets.size() == 0)
			return;

		BlockIndex b = targets.get(worldObj.rand.nextInt(targets.size()));
		laserTarget = (ILaserTarget) worldObj.getBlockTileEntity(b.i, b.j, b.k);
	}

	protected void createLaser() {

		laser = new EntityEnergyLaser(worldObj, new Position(xCoord, yCoord, zCoord), new Position(xCoord, yCoord, zCoord));
		worldObj.spawnEntityInWorld(laser);
	}

	protected void updateLaser() {

		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		double px = 0, py = 0, pz = 0;

		switch (ForgeDirection.values()[meta]) {

		case WEST:
			px = -0.3;
			break;
		case EAST:
			px = 0.3;
			break;
		case DOWN:
			py = -0.3;
			break;
		case UP:
			py = 0.3;
			break;
		case NORTH:
			pz = -0.3;
			break;
		case SOUTH:
		default:
			pz = 0.3;
			break;
		}

		Position head = new Position(xCoord + 0.5 + px, yCoord + 0.5 + py, zCoord + 0.5 + pz);
		Position tail = new Position(laserTarget.getXCoord() + 0.475 + (worldObj.rand.nextFloat() - 0.5) / 5F, laserTarget.getYCoord() + 9F / 16F,
				laserTarget.getZCoord() + 0.475 + (worldObj.rand.nextFloat() - 0.5) / 5F);

		laser.setPositions(head, tail);

		if (!laser.isVisible()) {
			laser.show();
		}
	}

	protected void removeLaser() {

		if (laser != null) {
			laser.setDead();
			laser = null;
		}
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public void doWork() {
	}

	@Override
	public int powerRequest() {
		if (powerProvider.getEnergyStored() < 200 || laser != null)
			return 25;
		else
			return 0;
	}

	@Override
	public void sendNetworkUpdate() {
		if (networkTracker.markTimeIfDelay(worldObj, nextNetworkUpdate)) {
			super.sendNetworkUpdate();
		}
	};

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		PowerFramework.currentFramework.loadPowerProvider(this, nbttagcompound);
		powerProvider.configure(20, 25, 25, 25, 1000);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		PowerFramework.currentFramework.savePowerProvider(this, nbttagcompound);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		removeLaser();
	}

}
