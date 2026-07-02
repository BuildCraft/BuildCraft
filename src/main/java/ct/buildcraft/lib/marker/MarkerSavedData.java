/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.marker;

import java.util.ArrayList;
import java.util.List;

import ct.buildcraft.api.core.BCLog;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;

public abstract class MarkerSavedData<S extends MarkerSubCache<C>, C extends MarkerConnection<C>> extends SavedData {
    protected static final boolean DEBUG_FULL = MarkerSubCache.DEBUG_FULL;
    
    public final String mapName;
    
    protected final List<BlockPos> markerPositions = new ArrayList<>();
    protected final List<List<BlockPos>> markerConnections = new ArrayList<>();
    private S subCache;
    
    public MarkerSavedData(String name) {
    	this.mapName = name;
	}
    
    public MarkerSavedData(CompoundTag nbt, String name) {
        mapName = name;
    	markerPositions.clear();
        markerConnections.clear();

        ListTag positionList = (ListTag) nbt.get("positions");
        int s = positionList.size();
        for (int i = 0; i < s; i++) {
            markerPositions.add(NbtUtils.readBlockPos((CompoundTag) positionList.get(i)));//CHANGED
        }

        ListTag connectionList = (ListTag) nbt.get("connections");
        int s1 = connectionList.size();
        for (int i = 0; i < s1; i++) {
            positionList = (ListTag) connectionList.get(i);
            List<BlockPos> inner = new ArrayList<>();
            markerConnections.add(inner);
            s = positionList.size();
            for (int j = 0; j < s; j++) {
                inner.add(NbtUtils.readBlockPos((CompoundTag) positionList.get(j)));
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
    public CompoundTag save(CompoundTag nbt) {
        markerPositions.clear();
        markerConnections.clear();

        markerPositions.addAll(subCache.getAllMarkers());
        for (C connection : subCache.getConnections()) {
            markerConnections.add(new ArrayList<>(connection.getMarkerPositions()));
        }
        ListTag positionList = new ListTag();
        for (BlockPos p : markerPositions) {
            positionList.add(NbtUtils.writeBlockPos(p));
        }
        nbt.put("positions", positionList);

        ListTag connectionList = new ListTag();
        for (List<BlockPos> connection : markerConnections) {
        	ListTag inner = new ListTag();
            for (BlockPos p : connection) {
                inner.add(NbtUtils.writeBlockPos(p));
            }
            connectionList.add(inner);
        }
        nbt.put("connections", connectionList);
        
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
    	if(subCache ==null)
    		return false;
    	return subCache.isDirty();
 //       return true;
    }

    @Override
	public void setDirty() {
    	if(subCache !=null)
    		subCache.setDirty(true);
	}

	@Override
	public void setDirty(boolean p_77761_) {
    	if(subCache !=null)
    		subCache.setDirty(p_77761_);
	}

	public final void setCache(S subCache) {
        this.subCache = subCache;
    }
}
