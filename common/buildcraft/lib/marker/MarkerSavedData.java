/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.marker;

import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.List;

public abstract class MarkerSavedData<S extends MarkerSubCache<C>, C extends MarkerConnection<C>> extends WorldSavedData {
    protected static final boolean DEBUG_FULL = MarkerSubCache.DEBUG_FULL;

    protected final List<BlockPos> markerPositions = new ArrayList<>();
    protected final List<List<BlockPos>> markerConnections = new ArrayList<>();
    private S subCache;

    public MarkerSavedData(String name) {
        super(name);
    }

    @Override
    public void load(CompoundNBT nbt) {
        markerPositions.clear();
        markerConnections.clear();

        ListNBT positionList = (ListNBT) nbt.get("positions");
        for (int i = 0; i < positionList.size(); i++) {
            markerPositions.add(NBTUtilBC.readBlockPos(positionList.get(i)));
        }

        ListNBT connectionList = (ListNBT) nbt.get("connections");
        for (int i = 0; i < connectionList.size(); i++) {
            positionList = (ListNBT) connectionList.get(i);
            List<BlockPos> inner = new ArrayList<>();
            markerConnections.add(inner);
            for (int j = 0; j < positionList.size(); j++) {
                inner.add(NBTUtilBC.readBlockPos(positionList.get(j)));
            }
        }

        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Reading from NBT (" + getId() + ")");
            BCLog.logger.info("[lib.marker.full]  - Positions:");
            for (BlockPos pos : markerPositions) {
                BCLog.logger.info("[lib.marker.full]   - " + pos);
            }
            BCLog.logger.info("[lib.marker.full]  - Connections:");
            for (List<BlockPos> list : markerConnections) {
                BCLog.logger.info("[lib.marker.full]   - Single NetworkManager:");
                for (BlockPos pos : list) {
                    BCLog.logger.info("[lib.marker.full]     - " + pos);
                }
            }
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        markerPositions.clear();
        markerConnections.clear();

        markerPositions.addAll(subCache.getAllMarkers());
        for (C connection : subCache.getConnections()) {
            markerConnections.add(new ArrayList<>(connection.getMarkerPositions()));
        }

        ListNBT positionList = new ListNBT();
        for (BlockPos p : markerPositions) {
            positionList.add(NBTUtilBC.writeBlockPos(p));
        }
        nbt.put("positions", positionList);

        ListNBT connectionList = new ListNBT();
        for (List<BlockPos> connection : markerConnections) {
            ListNBT inner = new ListNBT();
            for (BlockPos p : connection) {
                inner.add(NBTUtilBC.writeBlockPos(p));
            }
            connectionList.add(inner);
        }
        nbt.put("connections", connectionList);

        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Writing to NBT (" + getId() + ")");
            BCLog.logger.info("[lib.marker.full]  - Positions:");
            for (BlockPos pos : markerPositions) {
                BCLog.logger.info("[lib.marker.full]   - " + pos);
            }
            BCLog.logger.info("[lib.marker.full]  - Connections:");
            for (List<BlockPos> list : markerConnections) {
                BCLog.logger.info("[lib.marker.full]   - Single NetworkManager:");
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
