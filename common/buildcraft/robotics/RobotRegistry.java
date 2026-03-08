/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics;

import buildcraft.api.core.BCLog;
import buildcraft.api.robots.*;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.robotics.pipe.DockingStationPipe;
import io.netty.util.collection.LongObjectHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// public class RobotRegistry extends WorldSavedData implements IRobotRegistry
public class RobotRegistry extends WorldSavedData implements IRobotRegistry {

    protected World world;
    // protected final HashMap<StationIndex, DockingStation> stations = new HashMap<StationIndex, DockingStation>();
    protected final Map<StationIndex, DockingStation> stations = new ConcurrentHashMap<StationIndex, DockingStation>();

    private long nextRobotID = Long.MIN_VALUE;

    private final LongObjectHashMap<EntityRobot> robotsLoaded = new LongObjectHashMap<EntityRobot>();
    // private final HashSet<EntityRobot> robotsLoadedSet = new HashSet<EntityRobot>();
    private final HashSet<EntityRobot> robotsLoadedSet = new HashSet<EntityRobot>();
    private final HashMap<ResourceId, Long> resourcesTaken = new HashMap<ResourceId, Long>();
    private final LongObjectHashMap<HashSet<ResourceId>> resourcesTakenByRobot = new LongObjectHashMap<HashSet<ResourceId>>();
    private final LongObjectHashMap<HashSet<StationIndex>> stationsTakenByRobot = new LongObjectHashMap<HashSet<StationIndex>>();

    public RobotRegistry(String id) {
        super(id);
    }

    @Override
    public long getNextRobotId() {
        long result = nextRobotID;

        nextRobotID = nextRobotID + 1;

        return result;
    }

    @Override
    public void registerRobot(EntityRobotBase robot) {
        // markDirty();
        setDirty();

        if (robot.getRobotId() == EntityRobotBase.NULL_ROBOT_ID) {
            ((EntityRobot) robot).setUniqueRobotId(getNextRobotId());
        }
        // if (robotsLoaded.containsItem(robot.getRobotId()))
        if (robotsLoaded.containsKey(robot.getRobotId())) {
            BCLog.logger.warn("Robot with id %d was not unregistered properly", robot.getRobotId());
        }

        addRobotLoaded((EntityRobot) robot);
    }

    private HashSet<ResourceId> getResourcesTakenByRobot(long robotId) {
        return (HashSet<ResourceId>) resourcesTakenByRobot.get(robotId);
    }

    private HashSet<StationIndex> getStationsTakenByRobot(long robotId) {
        return (HashSet<StationIndex>) stationsTakenByRobot.get(robotId);
    }

    private void addRobotLoaded(EntityRobot robot) {
        robotsLoaded.put(robot.getRobotId(), robot);
        robotsLoadedSet.add(robot);
    }

    private void removeRobotLoaded(EntityRobot robot) {
        robotsLoaded.remove(robot.getRobotId());
        robotsLoadedSet.remove(robot);
    }

    @Override
    public void killRobot(EntityRobotBase robot) {
        // markDirty();
        setDirty();

        releaseResources(robot, true);
        removeRobotLoaded((EntityRobot) robot);
    }

    @Override
    public void unloadRobot(EntityRobotBase robot) {
        // markDirty();
        setDirty();

        releaseResources(robot, false, true);
        removeRobotLoaded((EntityRobot) robot);
    }

    @Override
    public EntityRobot getLoadedRobot(long id) {
        if (robotsLoaded.containsKey(id)) {
            return (EntityRobot) robotsLoaded.get(id);
        } else {
            return null;
        }
    }

    @Override
    public synchronized boolean isTaken(ResourceId resourceId) {
        return robotIdTaking(resourceId) != EntityRobotBase.NULL_ROBOT_ID;
    }

    @Override
    public synchronized long robotIdTaking(ResourceId resourceId) {
        if (!resourcesTaken.containsKey(resourceId)) {
            return EntityRobotBase.NULL_ROBOT_ID;
        }

        long robotId = resourcesTaken.get(resourceId);

        // if (robotsLoaded.containsKey(robotId) && !((EntityRobot) robotsLoaded.get(robotId)).isDead)
        if (robotsLoaded.containsKey(robotId) && robotsLoaded.get(robotId).isAlive()) {
            return robotId;
        } else {
            // If the robot is either not loaded or dead, the resource is not
            // actively used anymore. Release it.
            release(resourceId);
            return EntityRobotBase.NULL_ROBOT_ID;
        }
    }

    @Override
    public synchronized EntityRobot robotTaking(ResourceId resourceId) {
        long robotId = robotIdTaking(resourceId);

        if (robotId == EntityRobotBase.NULL_ROBOT_ID || !robotsLoaded.containsKey(robotId)) {
            return null;
        } else {
            return (EntityRobot) robotsLoaded.get(robotId);
        }
    }

    @Override
    public synchronized boolean take(ResourceId resourceId, EntityRobotBase robot) {
        // markDirty();
        setDirty();

        return take(resourceId, robot.getRobotId());
    }

    @Override
    public synchronized boolean take(ResourceId resourceId, long robotId) {
        if (resourceId == null) {
            return false;
        }

        // markDirty();
        setDirty();

        if (!resourcesTaken.containsKey(resourceId)) {
            resourcesTaken.put(resourceId, robotId);

            if (!resourcesTakenByRobot.containsKey(robotId)) {
                resourcesTakenByRobot.put(robotId, new HashSet<ResourceId>());
            }

            getResourcesTakenByRobot(robotId).add(resourceId);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void release(ResourceId resourceId) {
        if (resourceId == null) {
            return;
        }

        // markDirty();
        setDirty();

        if (resourcesTaken.containsKey(resourceId)) {
            long robotId = resourcesTaken.get(resourceId);

            getResourcesTakenByRobot(robotId).remove(resourceId);
            resourcesTaken.remove(resourceId);
        }
    }

    @Override
    public synchronized void releaseResources(EntityRobotBase robot) {
        releaseResources(robot, false);
    }

    private synchronized void releaseResources(EntityRobotBase robot, boolean forceAll) {
        releaseResources(robot, forceAll, false);
    }

    private synchronized void releaseResources(EntityRobotBase robot, boolean forceAll, boolean resetEntities) {
        // markDirty();
        setDirty();

        if (resourcesTakenByRobot.containsKey(robot.getRobotId())) {
            HashSet<ResourceId> resourceSet = (HashSet<ResourceId>) getResourcesTakenByRobot(robot.getRobotId()).clone();

            for (ResourceId id : resourceSet) {
                release(id);
            }

            resourcesTakenByRobot.remove(robot.getRobotId());
        }

        if (stationsTakenByRobot.containsKey(robot.getRobotId())) {
            HashSet<StationIndex> stationSet = (HashSet<StationIndex>) getStationsTakenByRobot(robot.getRobotId()).clone();

            for (StationIndex s : stationSet) {
                DockingStation d = stations.get(s);

                if (d != null) {
                    if (!d.canRelease()) {
                        if (forceAll) {
                            d.unsafeRelease(robot);
                        } else if (resetEntities && d.robotIdTaking() == robot.getRobotId()) {
                            d.invalidateRobotTakingEntity();
                        }
                    } else {
                        d.unsafeRelease(robot);
                    }
                }
            }

            if (forceAll) {
                stationsTakenByRobot.remove(robot.getRobotId());
            }
        }
    }

    @Override
    public synchronized DockingStation getStation(BlockPos pos, Direction side) {
        StationIndex index = new StationIndex(side, pos);

        if (stations.containsKey(index)) {
            return stations.get(index);
        } else {
            return null;
        }
    }

    @Override
    public synchronized Collection<DockingStation> getStations() {
        return stations.values();
    }

    @Override
    public synchronized void registerStation(DockingStation station) {
        // markDirty();
        setDirty();

        StationIndex index = new StationIndex(station);

        if (stations.containsKey(index)) {
            throw new InvalidParameterException("Station " + index + " already registered");
        } else {
            stations.put(index, station);
        }
    }

    @Override
    public synchronized void removeStation(DockingStation station) {
        // markDirty();
        setDirty();

        StationIndex index = new StationIndex(station);

        if (stations.containsKey(index)) {
            if (station.robotTaking() != null) {
                if (!station.isMainStation()) {
                    station.robotTaking().undock();
                } else {
                    station.robotTaking().setMainStation(null);
                }
            } else if (station.robotIdTaking() != EntityRobotBase.NULL_ROBOT_ID) {
                if (stationsTakenByRobot.containsKey(station.robotIdTaking())) {
                    getStationsTakenByRobot(station.robotIdTaking()).remove(index);
                }
            }

            stations.remove(index);
        }
    }

    @Override
    public synchronized void take(DockingStation station, long robotId) {
        if (!stationsTakenByRobot.containsKey(robotId)) {
            stationsTakenByRobot.put(robotId, new HashSet<StationIndex>());
        }

        getStationsTakenByRobot(robotId).add(new StationIndex(station));
    }

    @Override
    public synchronized void release(DockingStation station, long robotId) {
        if (stationsTakenByRobot.containsKey(robotId)) {
            getStationsTakenByRobot(robotId).remove(new StationIndex(station));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        writeToNBT(nbt);
        return nbt;
    }

    @Override
    public synchronized void writeToNBT(CompoundNBT nbt) {
        nbt.putLong("nextRobotID", nextRobotID);

        ListNBT resourceList = new ListNBT();

        for (Map.Entry<ResourceId, Long> e : resourcesTaken.entrySet()) {
            CompoundNBT cpt = new CompoundNBT();
            CompoundNBT resourceId = new CompoundNBT();
            e.getKey().writeToNBT(resourceId);
            cpt.put("resourceId", resourceId);
            cpt.putLong("robotId", e.getValue());

            resourceList.add(cpt);
        }

        nbt.put("resourceList", resourceList);

        ListNBT stationList = new ListNBT();

        for (Map.Entry<StationIndex, DockingStation> e : stations.entrySet()) {
            CompoundNBT cpt = new CompoundNBT();
            e.getValue().writeToNBT(cpt);
            cpt.putString("stationType", RobotManager.getDockingStationName(e.getValue().getClass()));
            stationList.add(cpt);
        }

        nbt.put("stationList", stationList);
    }

    @Override
    public void load(CompoundNBT nbt) {
        this.readFromNBT(nbt);
    }

    @Override
    // public synchronized void readFromNBT(CompoundNBT nbt)
    public synchronized void readFromNBT(CompoundNBT nbt) {
        nextRobotID = nbt.getLong("nextRobotID");

        ListNBT resourceList = nbt.getList("resourceList", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < resourceList.size(); ++i) {
            CompoundNBT cpt = resourceList.getCompound(i);
            ResourceId resourceId = ResourceId.load(cpt.getCompound("resourceId"));
            long robotId = cpt.getLong("robotId");

            take(resourceId, robotId);
        }

        ListNBT stationList = nbt.getList("stationList", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < stationList.size(); ++i) {
            CompoundNBT cpt = stationList.getCompound(i);

            Class<? extends DockingStation> cls;

            if (!cpt.contains("stationType")) {
                cls = DockingStationPipe.class;
            } else {
                cls = RobotManager.getDockingStationByName(cpt.getString("stationType"));
                if (cls == null) {
                    BCLog.logger.error("Could not load docking station of type " + nbt.getString("stationType"));
                    continue;
                }
            }

            try {
                DockingStation station = cls.newInstance();
                station.readFromNBT(cpt);

                registerStation(station);

                if (station.linkedId() != EntityRobotBase.NULL_ROBOT_ID) {
                    take(station, station.linkedId());
                }
            } catch (Exception e) {
                BCLog.logger.error("Could not load docking station", e);
            }
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload e) {
        if (e.getWorld() == this.world) {
            for (EntityRobot robot : new ArrayList<EntityRobot>(robotsLoadedSet)) {
                // if (!e.getWorld().loadedEntityList.contains(robot))
                if (e.getWorld() instanceof World && !((ServerWorld) e.getWorld()).getChunkSource().chunkMap.entityMap.values().stream().anyMatch(trackedEntity -> trackedEntity.entity == robot)) {
                    robot.onChunkUnload();
                }
            }
            for (DockingStation station : new ArrayList<DockingStation>(stations.values())) {
                // if (!world.isBlockLoaded(station.getPos()))
                if (!world.isLoaded(station.getPos())) {
                    station.onChunkUnload();
                }
            }
        }
    }

    /** This function is a wrapper for markDirty(), done this way due to obfuscation issues. */
    @Override
    public void registryMarkDirty() {
        // markDirty();
        setDirty();
    }
}
