/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.transport.IPipeTile;

public class DockingStation {
	public ForgeDirection side;
	public World world;

	private long robotTakingId = EntityRobotBase.NULL_ROBOT_ID;
	private EntityRobotBase robotTaking;

	private boolean linkIsMain = false;

	private BlockIndex index;
	private IPipeTile pipe;

	public DockingStation(BlockIndex iIndex, ForgeDirection iSide) {
		index = iIndex;
		side = iSide;
	}

	public DockingStation(IPipeTile iPipe, ForgeDirection iSide) {
		index = new BlockIndex(iPipe.x(), iPipe.y(), iPipe.z());
		pipe = iPipe;
		side = iSide;
		world = iPipe.getWorld();
	}

	public DockingStation() {
	}

	public boolean isMainStation() {
		return linkIsMain;
	}

	public IPipeTile getPipe() {
		if (pipe == null) {
			pipe = (IPipeTile) world.getTileEntity(index.x, index.y, index.z);
		}

		if (pipe == null || ((TileEntity) pipe).isInvalid()) {
			// Inconsistency - remove this pipe from the registry.
			RobotManager.registryProvider.getRegistry(world).removeStation(this);
			pipe = null;
		}

		return pipe;
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
			robotTaking = RobotManager.registryProvider.getRegistry(world).getLoadedRobot(robotTakingId);
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
			getPipe().scheduleRenderUpdate();
			registry.markDirty();
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
			getPipe().scheduleRenderUpdate();
			registry.markDirty();
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
			registry.markDirty();
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
			getPipe().scheduleRenderUpdate();
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
		index = new BlockIndex (nbt.getCompoundTag("index"));
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

