/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraft.util.EnumFacing;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.core.utils.Utils;
import buildcraft.transport.TileGenericPipe;

public class DockingStation implements IDockingStation {
	public EnumFacing side;
	public World world;

	private long robotTakingId = EntityRobotBase.NULL_ROBOT_ID;
	private EntityRobotBase robotTaking;

	private boolean linkIsMain = false;

	private BlockPos index;
	private TileGenericPipe pipe;

	public DockingStation(BlockPos iIndex, EnumFacing iSide) {
		index = iIndex;
		side = iSide;
	}

	public DockingStation(TileGenericPipe iPipe, EnumFacing iSide) {
		index = new BlockPos(iPipe.getPos());
		pipe = iPipe;
		side = iSide;
		world = iPipe.getWorld();
	}

	public DockingStation() {
	}

	public boolean isMainStation() {
		return linkIsMain;
	}

	public TileGenericPipe getPipe() {
		if (pipe == null) {
			pipe = (TileGenericPipe) world.getTileEntity(index);
		}

		if (pipe == null || pipe.isInvalid()) {
			// Inconsistency - remove this pipe from the registry.
			RobotRegistry.getRegistry(world).removeStation(this);
			pipe = null;
		}

		return pipe;
	}

	@Override
	public BlockPos pos() {
		return index;
	}

	@Override
	public EnumFacing side() {
		return side;
	}

	@Override
	public EntityRobotBase robotTaking() {
		if (robotTakingId == EntityRobotBase.NULL_ROBOT_ID) {
			return null;
		} else if (robotTaking == null) {
			robotTaking = RobotRegistry.getRegistry(world).getLoadedRobot(robotTakingId);
		}

		return robotTaking;
	}

	@Override
	public long linkedId() {
		return robotTakingId;
	}

	public boolean takeAsMain(EntityRobotBase robot) {
		if (robotTakingId == EntityRobotBase.NULL_ROBOT_ID) {
			linkIsMain = true;
			robotTaking = robot;
			robotTakingId = robot.getRobotId();
			getPipe().scheduleRenderUpdate();
			RobotRegistry.getRegistry(world).markDirty();
			((EntityRobot) robot).setMainStation(this);
			RobotRegistry.getRegistry(world).take(this, robot.getRobotId());

			return true;
		} else {
			return robotTakingId == robot.getRobotId();
		}
	}

	@Override
	public boolean take(EntityRobotBase robot) {
		if (robotTaking == null) {
			linkIsMain = false;
			robotTaking = robot;
			robotTakingId = robot.getRobotId();
			getPipe().scheduleRenderUpdate();
			RobotRegistry.getRegistry(world).markDirty();
			RobotRegistry.getRegistry(world).take(this, robot.getRobotId());

			return true;
		} else {
			return robot.getRobotId() == robotTakingId;
		}
	}

	public void release(EntityRobotBase robot) {
		if (robotTaking == robot && !linkIsMain) {
			linkIsMain = false;
			robotTaking = null;
			robotTakingId = EntityRobotBase.NULL_ROBOT_ID;
			getPipe().scheduleRenderUpdate();
			RobotRegistry.getRegistry(world).markDirty();
			RobotRegistry.getRegistry(world).release(this, robot.getRobotId());
		}
	}

	/**
	 * Same a release but doesn't clear the registry (presumably called from the
	 * registry).
	 */
	public void unsafeRelease(EntityRobotBase robot) {
		if (robotTaking == robot) {
			robotTaking = null;
			robotTakingId = EntityRobotBase.NULL_ROBOT_ID;
			getPipe().scheduleRenderUpdate();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound indexNBT = new NBTTagCompound();
		Utils.writeBlockPos(indexNBT, index);
		nbt.setTag("index", indexNBT);
		nbt.setByte("side", (byte) side.ordinal());
		nbt.setBoolean("isMain", linkIsMain);
		nbt.setLong("robotId", robotTakingId);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		index = Utils.readBlockPos(nbt.getCompoundTag("index"));
		side = EnumFacing.values()[nbt.getByte("side")];
		linkIsMain = nbt.getBoolean("isMain");
		robotTakingId = nbt.getLong("robotId");
	}

	@Override
	public boolean isTaken() {
		return robotTakingId != EntityRobotBase.NULL_ROBOT_ID;
	}

	@Override
	public long robotIdTaking() {
		return robotTakingId;
	}

	@Override
	public String toString () {
		return "{" + index.getX() + ", " + index.getY() + ", " + index.getZ() + ", " + side + " :" + robotTakingId + "}";
	}

	public boolean linkIsDocked() {
		if (isTaken()) {
			return robotTaking().getDockingStation() == this;
		} else {
			return false;
		}
	}

	public boolean canRelease() {
		return !isMainStation() && !linkIsDocked();
	}
}

