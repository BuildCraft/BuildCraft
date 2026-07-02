package ct.buildcraft.robotics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.IRobotRegistry;
import ct.buildcraft.api.robots.IRobotRegistryProvider;
import ct.buildcraft.api.robots.ResourceId;
import ct.buildcraft.api.robots.RobotManager;
import ct.buildcraft.robotics.entity.EntityRobot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public enum SimpleRobotRegistryProvider implements IRobotRegistryProvider {
    INSTANCE;

    private final Map<Level, SimpleRobotRegistry> clientRegistries = new WeakHashMap<>();

    @Override
    public IRobotRegistry getRegistry(Level level) {
        if (level instanceof ServerLevel serverLevel && !level.isClientSide) {
            return SimpleRobotRegistry.get(serverLevel);
        }

        return clientRegistries.computeIfAbsent(level, SimpleRobotRegistry::new);
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.isClientSide) {
            return;
        }

        SimpleRobotRegistry.get(level).onChunkUnload(event.getChunk().getPos());
    }

    private static final class SimpleRobotRegistry extends SavedData implements IRobotRegistry {
        private static final String DATA_NAME = "buildcraft_robotics_registry";

        @Nullable
        private Level level;
        private final Map<Long, EntityRobotBase> robots = new HashMap<>();
        private final Map<ResourceId, Long> resources = new HashMap<>();
        private final Map<StationKey, DockingStation> stations = new LinkedHashMap<>();
        private long nextRobotId = 1;

        private SimpleRobotRegistry() {
        }

        private SimpleRobotRegistry(@Nullable Level level) {
            setLevel(level);
        }

        private static SimpleRobotRegistry get(ServerLevel level) {
            DimensionDataStorage storage = level.getDataStorage();
            SimpleRobotRegistry registry = storage.computeIfAbsent(SimpleRobotRegistry::load, SimpleRobotRegistry::new, DATA_NAME);
            registry.setLevel(level);
            return registry;
        }

        private static SimpleRobotRegistry load(CompoundTag nbt) {
            SimpleRobotRegistry registry = new SimpleRobotRegistry();
            registry.readFromNBT(nbt);
            return registry;
        }

        private void setLevel(@Nullable Level level) {
            this.level = level;
            for (DockingStation station : stations.values()) {
                station.setLevel(level);
            }
        }

        @Override
        public long getNextRobotId() {
            registryMarkDirty();
            return nextRobotId++;
        }

        @Override
        public void registerRobot(EntityRobotBase robot) {
            if (robot == null) {
                return;
            }

            if (robot.getRobotId() == EntityRobotBase.NULL_ROBOT_ID) {
                if (robot instanceof EntityRobot entityRobot) {
                    entityRobot.setUniqueRobotId(getNextRobotId());
                } else {
                    return;
                }
            }

            robots.put(robot.getRobotId(), robot);
            registryMarkDirty();
        }

        @Override
        public void killRobot(EntityRobotBase robot) {
            if (robot != null) {
                releaseResources(robot, true, false);
                robots.remove(robot.getRobotId());
                registryMarkDirty();
            }
        }

        @Override
        public void unloadRobot(EntityRobotBase robot) {
            if (robot != null) {
                releaseResources(robot, false, true);
                robots.remove(robot.getRobotId());
                registryMarkDirty();
            }
        }

        @Override
        public EntityRobotBase getLoadedRobot(long id) {
            return robots.get(id);
        }

        @Override
        public boolean isTaken(ResourceId resourceId) {
            return robotIdTaking(resourceId) != EntityRobotBase.NULL_ROBOT_ID;
        }

        @Override
        public long robotIdTaking(ResourceId resourceId) {
            Long robotId = resources.get(resourceId);
            if (robotId == null) {
                return EntityRobotBase.NULL_ROBOT_ID;
            }

            // Station reservations intentionally survive robot entity unloads. Generic resources do not.
            if (resourceId instanceof StationResourceId) {
                return robotId;
            }

            EntityRobotBase robot = robots.get(robotId);
            if (robot == null || !robot.isAlive()) {
                release(resourceId);
                return EntityRobotBase.NULL_ROBOT_ID;
            }
            return robotId;
        }

        @Override
        public EntityRobotBase robotTaking(ResourceId resourceId) {
            long robotId = robotIdTaking(resourceId);
            return robotId == EntityRobotBase.NULL_ROBOT_ID ? null : robots.get(robotId);
        }

        @Override
        public boolean take(ResourceId resourceId, EntityRobotBase robot) {
            if (robot == null) return false;
            return take(resourceId, robot.getRobotId());
        }

        @Override
        public boolean take(ResourceId resourceId, long robotId) {
            if (resourceId == null || robotId == EntityRobotBase.NULL_ROBOT_ID) {
                return false;
            }

            Long current = resources.get(resourceId);
            if (current == null || current == robotId) {
                resources.put(resourceId, robotId);
                registryMarkDirty();
                return true;
            }
            return false;
        }

        @Override
        public void release(ResourceId resourceId) {
            if (resourceId != null && resources.remove(resourceId) != null) {
                registryMarkDirty();
            }
        }

        @Override
        public void releaseResources(EntityRobotBase robot) {
            releaseResources(robot, false, false);
        }

        private void releaseResources(EntityRobotBase robot, boolean forceAll, boolean resetEntities) {
            if (robot == null) return;
            long robotId = robot.getRobotId();

            resources.entrySet().removeIf(entry -> !(entry.getKey() instanceof StationResourceId) && entry.getValue() == robotId);
            releaseStations(robot, forceAll, resetEntities);
            registryMarkDirty();
        }

        private void releaseStations(EntityRobotBase robot, boolean forceAll, boolean resetEntities) {
            if (robot == null) return;
            long robotId = robot.getRobotId();
            for (DockingStation station : stations.values()) {
                if (station == null || station.robotIdTaking() != robotId) {
                    continue;
                }

                if (forceAll || station.canRelease()) {
                    station.unsafeRelease(robot);
                    resources.remove(new StationResourceId(station), robotId);
                } else if (resetEntities) {
                    station.invalidateRobotTakingEntity();
                }
            }
        }

        @Override
        public DockingStation getStation(int x, int y, int z, @Nullable Direction side) {
            return stations.get(new StationKey(new BlockPos(x, y, z), side));
        }

        @Override
        public Collection<DockingStation> getStations() {
            return Collections.unmodifiableCollection(stations.values());
        }

        @Override
        public void registerStation(DockingStation station) {
            if (station == null) return;
            putStation(station);
            if (station.linkedId() != EntityRobotBase.NULL_ROBOT_ID) {
                resources.put(new StationResourceId(station), station.linkedId());
            }
            registryMarkDirty();
        }

        private void putStation(DockingStation station) {
            station.setLevel(level);
            stations.put(new StationKey(new BlockPos(station.x(), station.y(), station.z()), station.side()), station);
        }

        @Override
        public void removeStation(DockingStation station) {
            if (station == null) return;

            EntityRobotBase robot = station.robotTaking();
            if (robot != null) {
                // A destroyed station can be both the robot's home station and its current dock. The old port only
                // cleared one of those references, which left robots snapped to a phantom station after explosions.
                if (robot.getDockingStation() == station) {
                    robot.undock();
                }
                if (station.isMainStation() || robot.getLinkedStation() == station) {
                    robot.setMainStation(null);
                }
                station.unsafeRelease(robot);
            }

            resources.entrySet().removeIf(entry -> entry.getKey() instanceof StationResourceId id && id.matches(station));
            stations.remove(new StationKey(new BlockPos(station.x(), station.y(), station.z()), station.side()));
            registryMarkDirty();
        }

        @Override
        public void take(DockingStation station, long robotId) {
            if (station != null && robotId != EntityRobotBase.NULL_ROBOT_ID) {
                resources.put(new StationResourceId(station), robotId);
                registryMarkDirty();
            }
        }

        @Override
        public void release(DockingStation station, long robotId) {
            if (station != null && resources.remove(new StationResourceId(station), robotId)) {
                registryMarkDirty();
            }
        }

        @Override
        public CompoundTag save(CompoundTag nbt) {
            writeToNBT(nbt);
            return nbt;
        }

        @Override
        public void writeToNBT(CompoundTag nbt) {
            nbt.putLong("nextRobotId", nextRobotId);

            ListTag resourceList = new ListTag();
            for (Map.Entry<ResourceId, Long> entry : resources.entrySet()) {
                ResourceId resourceId = entry.getKey();
                if (resourceId instanceof StationResourceId || RobotManager.getResourceIdName(resourceId.getClass()) == null) {
                    continue;
                }

                CompoundTag resourceTag = new CompoundTag();
                resourceId.writeToNBT(resourceTag);

                CompoundTag entryTag = new CompoundTag();
                entryTag.put("resourceId", resourceTag);
                entryTag.putLong("robotId", entry.getValue());
                resourceList.add(entryTag);
            }
            nbt.put("resourceList", resourceList);

            ListTag stationList = new ListTag();
            for (DockingStation station : stations.values()) {
                if (station == null) {
                    continue;
                }
                String stationType = RobotManager.getDockingStationName(station.getClass());
                if (stationType == null) {
                    continue;
                }

                CompoundTag stationTag = new CompoundTag();
                station.writeToNBT(stationTag);
                stationTag.putString("stationType", stationType);
                stationList.add(stationTag);
            }
            nbt.put("stationList", stationList);
        }

        @Override
        public void readFromNBT(CompoundTag nbt) {
            robots.clear();
            resources.clear();
            stations.clear();

            if (nbt.contains("nextRobotId", Tag.TAG_LONG)) {
                nextRobotId = Math.max(1, nbt.getLong("nextRobotId"));
            } else if (nbt.contains("nextRobotID", Tag.TAG_LONG)) {
                nextRobotId = Math.max(1, nbt.getLong("nextRobotID"));
            } else {
                nextRobotId = 1;
            }

            ListTag resourceList = nbt.getList("resourceList", Tag.TAG_COMPOUND);
            for (int i = 0; i < resourceList.size(); i++) {
                CompoundTag entryTag = resourceList.getCompound(i);
                ResourceId resourceId = ResourceId.load(entryTag.getCompound("resourceId"));
                if (resourceId != null) {
                    resources.put(resourceId, entryTag.getLong("robotId"));
                }
            }

            ListTag stationList = nbt.getList("stationList", Tag.TAG_COMPOUND);
            for (int i = 0; i < stationList.size(); i++) {
                CompoundTag stationTag = stationList.getCompound(i);
                Class<? extends DockingStation> stationClass;
                if (!stationTag.contains("stationType", Tag.TAG_STRING)) {
                    stationClass = DockingStationPipe.class;
                } else {
                    stationClass = RobotManager.getDockingStationByName(stationTag.getString("stationType"));
                    if (stationClass == null) {
                        continue;
                    }
                }

                try {
                    DockingStation station = stationClass.getDeclaredConstructor().newInstance();
                    station.readFromNBT(stationTag);
                    putStation(station);
                    if (station.linkedId() != EntityRobotBase.NULL_ROBOT_ID) {
                        resources.put(new StationResourceId(station), station.linkedId());
                    }
                } catch (ReflectiveOperationException exception) {
                    ct.buildcraft.api.core.BCLog.logger.warn("Failed to load robot docking station from NBT", exception);
                }
            }
        }

        @Override
        public void registryMarkDirty() {
            setDirty();
        }

        private void onChunkUnload(ChunkPos chunkPos) {
            for (EntityRobotBase robot : new ArrayList<>(robots.values())) {
                if (robot != null && chunkPos.equals(robot.chunkPosition())) {
                    robot.onChunkUnload();
                }
            }

            for (DockingStation station : new ArrayList<>(stations.values())) {
                if (station == null) {
                    continue;
                }
                ChunkPos stationChunk = new ChunkPos(new BlockPos(station.x(), station.y(), station.z()));
                if (chunkPos.equals(stationChunk)) {
                    station.onChunkUnload();
                }
            }
        }
    }

    private record StationKey(BlockPos pos, @Nullable Direction side) {
    }

    private static final class StationResourceId extends ResourceId {
        private final BlockPos pos;
        @Nullable
        private final Direction side;

        private StationResourceId(DockingStation station) {
            this.pos = new BlockPos(station.x(), station.y(), station.z());
            this.side = station.side();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StationResourceId other)) return false;
            return pos.equals(other.pos) && side == other.side;
        }

        private boolean matches(DockingStation station) {
            return station != null
                    && pos.equals(new BlockPos(station.x(), station.y(), station.z()))
                    && side == station.side();
        }

        @Override
        public int hashCode() {
            return 31 * pos.hashCode() + (side == null ? 0 : side.hashCode());
        }
    }
}
