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
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.transport.TileGenericPipe;

public class DockingStation implements IDockingStation {
	public ForgeDirection side;
	public World world;

	private long robotTakingId = EntityRobotBase.NULL_ROBOT_ID;
	private EntityRobotBase robotTaking;

	private boolean linkIsMain = false;

	private BlockIndex index;
	private TileGenericPipe pipe;

	public DockingStation(BlockIndex iIndex, ForgeDirection iSide) {
		index = iIndex;
		side = iSide;
	}

	public DockingStation(TileGenericPipe iPipe, ForgeDirection iSide) {
		index = new BlockIndex(iPipe);
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
			pipe = (TileGenericPipe) world.getTileEntity(index.x, index.y, index.z);
		}

		if (pipe == null || pipe.isInvalid()) {
			// Inconsistency - remove this pipe from the registry.
			RobotRegistry.getRegistry(world).removeStation(this);
			pipe = null;
		}

		return pipe;
	}

	@Override
	public int x() {
		return index.x;
	}

	@Override
	public int y() {
		return index.y;
	}

	@Override
	public int z() {
		return index.z;
	}

	@Override
	public ForgeDirection side() {
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
		if (robotTaking == null) {
			linkIsMain = true;
			robotTaking = robot;
			robotTakingId = robot.getRobotId();
			getPipe().scheduleRenderUpdate();
			RobotRegistry.getRegistry(world).markDirty();
			((EntityRobot) robot).setMainStation(this);
			RobotRegistry.getRegistry(world).take(this, robot.getRobotId());

			return true;
		} else {
			return false;
		}
	}

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
		index.writeTo(indexNBT);
		nbt.setTag("index", indexNBT);
		nbt.setByte("side", (byte) side.ordinal());
		nbt.setBoolean("isMain", linkIsMain);
		nbt.setLong("robotId", robotTakingId);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		index = new BlockIndex (nbt.getCompoundTag("index"));
		side = ForgeDirection.values()[nbt.getByte("side")];
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
	public BlockIndex index() {
		return index;
	}

	@Override
	public String toString () {
		return "{" + index.x + ", " + index.y + ", " + index.z + ", " + side + " :" + robotTakingId + "}";
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

