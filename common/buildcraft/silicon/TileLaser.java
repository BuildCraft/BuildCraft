/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import buildcraft.api.power.ILaserTarget;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftMod;
import buildcraft.BuildCraftSilicon;
import buildcraft.api.core.Position;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.EntityEnergyLaser;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.ActionMachineControl;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileLaser extends TileBuildCraft implements IPowerReceptor, IActionReceptor, IMachine {

	private static final float LASER_OFFSET = 2.0F / 16.0F;
	private EntityEnergyLaser laser = null;
	private final SafeTimeTracker laserTickTracker = new SafeTimeTracker(10);
	private final SafeTimeTracker searchTracker = new SafeTimeTracker(100, 100);
	private final SafeTimeTracker networkTracker = new SafeTimeTracker(3);
	private ILaserTarget laserTarget;
	protected PowerHandler powerHandler;
	private ActionMachineControl.Mode lastMode = ActionMachineControl.Mode.Unknown;
	private static final PowerHandler.PerditionCalculator PERDITION = new PowerHandler.PerditionCalculator(0.5F);

	public TileLaser() {
		powerHandler = new PowerHandler(this, Type.MACHINE);
		initPowerProvider();
	}

	private void initPowerProvider() {
		powerHandler.configure(25, 150, 25, 1000);
		powerHandler.setPerdition(PERDITION);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (!!worldObj.isRemote)
			return;

		// If a gate disabled us, remove laser and do nothing.
		if (lastMode == ActionMachineControl.Mode.Off) {
			removeLaser();
			return;
		}

		// Check for any available tables at a regular basis
		if (canFindTable()) {
			findTable();
		}

		// If we still don't have a valid table or the existing has
		// become invalid, we disable the laser and do nothing.
		if (!isValidTable()) {
			removeLaser();
			return;
		}

		// Disable the laser and do nothing if no energy is available.
		if (powerHandler.getEnergyStored() == 0) {
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
		double power = powerHandler.useEnergy(0, getMaxPowerSent(), true);
		laserTarget.receiveLaserEnergy(power);

		if (laser != null) {
			laser.pushPower(power);
		}

		onPowerSent(power);

		sendNetworkUpdate();
	}

	protected float getMaxPowerSent() {
		return 4;
	}

	protected void onPowerSent(double power) {
	}

	protected boolean canFindTable() {
		return searchTracker.markTimeIfDelay(worldObj);
	}

	protected boolean canUpdateLaser() {
		return laserTickTracker.markTimeIfDelay(worldObj);
	}

	protected boolean isValidTable() {

		if (laserTarget == null || laserTarget.isInvalidTarget() || !laserTarget.requiresLaserEnergy())
			return false;

		return true;
	}

	protected void findTable() {
		int meta = getBlockMetadata();

		int minX = xCoord - 5;
		int minY = yCoord - 5;
		int minZ = zCoord - 5;
		int maxX = xCoord + 5;
		int maxY = yCoord + 5;
		int maxZ = zCoord + 5;

		switch (ForgeDirection.getOrientation(meta)) {
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

		List<ILaserTarget> targets = new LinkedList<ILaserTarget>();

		for (int x = minX; x <= maxX; ++x) {
			for (int y = minY; y <= maxY; ++y) {
				for (int z = minZ; z <= maxZ; ++z) {

					TileEntity tile = worldObj.getTileEntity(x, y, z);
					if (tile instanceof ILaserTarget) {

						ILaserTarget table = (ILaserTarget) tile;
						if (table.requiresLaserEnergy()) {
							targets.add(table);
						}
					}

				}
			}
		}

		if (targets.isEmpty())
			return;

		laserTarget = targets.get(worldObj.rand.nextInt(targets.size()));
	}

	protected void createLaser() {

		laser = new EntityEnergyLaser(worldObj, new Position(xCoord, yCoord, zCoord), new Position(xCoord, yCoord, zCoord));
		worldObj.spawnEntityInWorld(laser);
	}

	protected void updateLaser() {

		int meta = getBlockMetadata();
		double px = 0, py = 0, pz = 0;

		switch (ForgeDirection.getOrientation(meta)) {

			case WEST:
				px = -LASER_OFFSET;
				break;
			case EAST:
				px = LASER_OFFSET;
				break;
			case DOWN:
				py = -LASER_OFFSET;
				break;
			case UP:
				py = LASER_OFFSET;
				break;
			case NORTH:
				pz = -LASER_OFFSET;
				break;
			case SOUTH:
			default:
				pz = LASER_OFFSET;
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
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {
	}

	@Override
	public void sendNetworkUpdate() {
		if (networkTracker.markTimeIfDelay(worldObj)) {
			super.sendNetworkUpdate();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		powerHandler.readFromNBT(nbttagcompound);
		initPowerProvider();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		powerHandler.writeToNBT(nbttagcompound);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		removeLaser();
	}

	@Override
	public boolean isActive() {
		return isValidTable();
	}

	@Override
	public boolean manageFluids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return false;
	}

	@Override
	public boolean allowAction(IAction action) {
		return action == BuildCraftCore.actionOn || action == BuildCraftCore.actionOff;
	}

	@Override
	public void actionActivated(IAction action) {
		if (action == BuildCraftCore.actionOn) {
			lastMode = ActionMachineControl.Mode.On;
		} else if (action == BuildCraftCore.actionOff) {
			lastMode = ActionMachineControl.Mode.Off;
		}
	}
}
