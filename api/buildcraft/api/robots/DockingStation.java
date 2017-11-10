/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IInjectable;

public abstract class DockingStation {
	public ForgeDirection side;
	public World world;

	private long robotTakingId = EntityRobotBase.NULL_ROBOT_ID;
	private EntityRobotBase robotTaking;

	private boolean linkIsMain = false;

	private BlockIndex index;

	public DockingStation(BlockIndex iIndex, ForgeDirection iSide) {
		index = iIndex;
		side = iSide;
	}

	public DockingStation() {
	}

	public boolean isMainStation() {
		return linkIsMain;
	}

	public int x() {
		return index.x;
	}

	public int y() {
		return index.y;
	}

	public int z() {
		return index.z;
	}

	public ForgeDirection side() {
		return side;
	}

	public EntityRobotBase robotTaking() {
		if (robotTakingId == EntityRobotBase.NULL_ROBOT_ID) {
			return null;
		} else if (robotTaking == null) {
			robotTaking = RobotManager.registryProvider.getRegistry(world).getLoadedRobot(
					robotTakingId);
		}

		return robotTaking;
	}

	public void invalidateRobotTakingEntity() {
		robotTaking = null;
	}

	public long linkedId() {
		return robotTakingId;
	}

	public boolean takeAsMain(EntityRobotBase robot) {
		if (robotTakingId == EntityRobotBase.NULL_ROBOT_ID) {
			IRobotRegistry registry = RobotManager.registryProvider.getRegistry(world);
			linkIsMain = true;
			robotTaking = robot;
			robotTakingId = robot.getRobotId();
			registry.registryMarkDirty();
			robot.setMainStation(this);
			registry.take(this, robot.getRobotId());

			return true;
		} else {
			return robotTakingId == robot.getRobotId();
		}
	}

	public boolean take(EntityRobotBase robot) {
		if (robotTaking == null) {
			IRobotRegistry registry = RobotManager.registryProvider.getRegistry(world);
			linkIsMain = false;
			robotTaking = robot;
			robotTakingId = robot.getRobotId();
			registry.registryMarkDirty();
			registry.take(this, robot.getRobotId());

			return true;
		} else {
			return robot.getRobotId() == robotTakingId;
		}
	}

	public void release(EntityRobotBase robot) {
		if (robotTaking == robot && !linkIsMain) {
			IRobotRegistry registry = RobotManager.registryProvider.getRegistry(world);
			unsafeRelease(robot);
			registry.registryMarkDirty();
			registry.release(this, robot.getRobotId());
		}
	}

	/**
	 * Same a release but doesn't clear the registry (presumably called from the
	 * registry).
	 */
	public void unsafeRelease(EntityRobotBase robot) {
		if (robotTaking == robot) {
			linkIsMain = false;
			robotTaking = null;
			robotTakingId = EntityRobotBase.NULL_ROBOT_ID;
		}
	}

	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound indexNBT = new NBTTagCompound();
		index.writeTo(indexNBT);
		nbt.setTag("index", indexNBT);
		nbt.setByte("side", (byte) side.ordinal());
		nbt.setBoolean("isMain", linkIsMain);
		nbt.setLong("robotId", robotTakingId);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		index = new BlockIndex(nbt.getCompoundTag("index"));
		side = ForgeDirection.values()[nbt.getByte("side")];
		linkIsMain = nbt.getBoolean("isMain");
		robotTakingId = nbt.getLong("robotId");
	}

	public boolean isTaken() {
		return robotTakingId != EntityRobotBase.NULL_ROBOT_ID;
	}

	public long robotIdTaking() {
		return robotTakingId;
	}

	public BlockIndex index() {
		return index;
	}

	@Override
	public String toString() {
		return "{" + index.x + ", " + index.y + ", " + index.z + ", " + side + " :" + robotTakingId
				+ "}";
	}

	public boolean linkIsDocked() {
		if (robotTaking() != null) {
			return robotTaking().getDockingStation() == this;
		} else {
			return false;
		}
	}

	public boolean canRelease() {
		return !isMainStation() && !linkIsDocked();
	}

	public boolean isInitialized() {
		return true;
	}

	public abstract Iterable<StatementSlot> getActiveActions();

	public IInjectable getItemOutput() {
		return null;
	}

	public ForgeDirection getItemOutputSide() {
		return ForgeDirection.UNKNOWN;
	}

	public IInventory getItemInput() {
		return null;
	}

	public ForgeDirection getItemInputSide() {
		return ForgeDirection.UNKNOWN;
	}

	public IFluidHandler getFluidOutput() {
		return null;
	}

	public ForgeDirection getFluidOutputSide() {
		return ForgeDirection.UNKNOWN;
	}

	public IFluidHandler getFluidInput() {
		return null;
	}

	public ForgeDirection getFluidInputSide() {
		return ForgeDirection.UNKNOWN;
	}

	public boolean providesPower() {
		return false;
	}

	public IRequestProvider getRequestProvider() {
		return null;
	}

	public void onChunkUnload() {

	}
}
