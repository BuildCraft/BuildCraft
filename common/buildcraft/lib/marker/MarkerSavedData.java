package buildcraft.lib.marker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldSavedData;

import buildcraft.lib.misc.NBTUtils;

public abstract class MarkerSavedData<S extends MarkerSubCache<C>, C extends MarkerConnection<C>> extends WorldSavedData {
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
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList positionList = new NBTTagList();
        for (BlockPos p : subCache.getAllMarkers()) {
            positionList.appendTag(NBTUtils.writeBlockPos(p));
        }
        nbt.setTag("positions", positionList);

        NBTTagList connectionList = new NBTTagList();
        for (C connection : subCache.getConnections()) {
            NBTTagList inner = new NBTTagList();
            for (BlockPos p : connection.getMarkerPositions()) {
                inner.appendTag(NBTUtils.writeBlockPos(p));
            }
            connectionList.appendTag(inner);
        }
        nbt.setTag("connections", connectionList);

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
