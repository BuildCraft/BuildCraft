/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.robots;

import java.util.Collection;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

public interface IRobotRegistry {

	long getNextRobotId();

	void registerRobot(EntityRobotBase robot);

	void killRobot(EntityRobotBase robot);

	void unloadRobot(EntityRobotBase robot);

	EntityRobotBase getLoadedRobot(long id);

	boolean isTaken(ResourceId resourceId);

	long robotIdTaking(ResourceId resourceId);

	EntityRobotBase robotTaking(ResourceId resourceId);

	boolean take(ResourceId resourceId, EntityRobotBase robot);

	boolean take(ResourceId resourceId, long robotId);

	void release(ResourceId resourceId);

	void releaseResources(EntityRobotBase robot);

	IDockingStation getStation(int x, int y, int z, ForgeDirection side);

	Collection<IDockingStation> getStations();

	void registerStation(IDockingStation station);

	void removeStation(IDockingStation station);

	void take(IDockingStation station, long robotId);

	void release(IDockingStation station, long robotId);

	void writeToNBT(NBTTagCompound nbt);

	void readFromNBT(NBTTagCompound nbt);
}
