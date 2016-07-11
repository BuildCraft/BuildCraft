package buildcraft.lib.marker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldSavedData;

import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.NBTUtils;

public abstract class MarkerSavedData<S extends MarkerSubCache<C>, C extends MarkerConnection<C>> extends WorldSavedData {
    protected static final boolean DEBUG_FULL = MarkerSubCache.DEBUG_FULL;

    protected final List<BlockPos> markerPositions = new ArrayList<>();
    protected final List<List<BlockPos>> markerConnections = new ArrayList<>();
    private S subCache;

    public MarkerSavedData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        markerPositions.clear();
        markerConnections.clear();

        NBTTagList positionList = (NBTTagList) nbt.getTag("positions");
        for (int i = 0; i < positionList.tagCount(); i++) {
            markerPositions.add(NBTUtils.readBlockPos(positionList.get(i)));
        }

        NBTTagList connectionList = (NBTTagList) nbt.getTag("connections");
        for (int i = 0; i < connectionList.tagCount(); i++) {
            positionList = (NBTTagList) connectionList.get(i);
            List<BlockPos> inner = new ArrayList<>();
            markerConnections.add(inner);
            for (int j = 0; j < positionList.tagCount(); j++) {
                inner.add(NBTUtils.readBlockPos(positionList.get(j)));
            }
        }

        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Reading from NBT (" + mapName + ")");
            BCLog.logger.info("[lib.marker.full]  - Positions:");
            for (BlockPos pos : markerPositions) {
                BCLog.logger.info("[lib.marker.full]   - " + pos);
            }
            BCLog.logger.info("[lib.marker.full]  - Connections:");
            for (List<BlockPos> list : markerConnections) {
                BCLog.logger.info("[lib.marker.full]   - Single Connection:");
                for (BlockPos pos : list) {
                    BCLog.logger.info("[lib.marker.full]     - " + pos);
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        markerPositions.clear();
        markerConnections.clear();

        markerPositions.addAll(subCache.getAllMarkers());
        for (C connection : subCache.getConnections()) {
            markerConnections.add(new ArrayList<>(connection.getMarkerPositions()));
        }

        NBTTagList positionList = new NBTTagList();
        for (BlockPos p : markerPositions) {
            positionList.appendTag(NBTUtils.writeBlockPos(p));
        }
        nbt.setTag("positions", positionList);

        NBTTagList connectionList = new NBTTagList();
        for (List<BlockPos> connection : markerConnections) {
            NBTTagList inner = new NBTTagList();
            for (BlockPos p : connection) {
                inner.appendTag(NBTUtils.writeBlockPos(p));
            }
            connectionList.appendTag(inner);
        }
        nbt.setTag("connections", connectionList);

        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Writing to NBT (" + mapName + ")");
            BCLog.logger.info("[lib.marker.full]  - Positions:");
            for (BlockPos pos : markerPositions) {
                BCLog.logger.info("[lib.marker.full]   - " + pos);
            }
            BCLog.logger.info("[lib.marker.full]  - Connections:");
            for (List<BlockPos> list : markerConnections) {
                BCLog.logger.info("[lib.marker.full]   - Single Connection:");
                for (BlockPos pos : list) {
                    BCLog.logger.info("[lib.marker.full]     - " + pos);
                }
            }
        }

        return nbt;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public final void setCache(S subCache) {
        this.subCache = subCache;
    }
}
