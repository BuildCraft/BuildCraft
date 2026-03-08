/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.world.gen;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;
import java.util.Map.Entry;

//public class RetroGenData extends WorldSavedData
public class RetroGenData extends SavedData {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.gen.retro");
    public static final String NAME = "buildcraft_world_gen";
    private final Map<ChunkPos, Set<String>> gennedChunks = new HashMap<>();

    public RetroGenData() {
        this(NAME);
    }

    public RetroGenData(String name) {
//        super(name);
    }

    // @Override
    public void readFromNBT(CompoundTag nbt) {
        gennedChunks.clear();

        ListTag registry = nbt.getList("registry", Tag.TAG_STRING);
        String[] names = new String[registry.size()];
        for (int i = 0; i < registry.size(); i++) {
            names[i] = registry.getString(i);
        }

        if (DEBUG) {
            BCLog.logger.info("[lib.gen.retro] Read registry!");
            for (int i = 0; i < names.length; i++) {
                BCLog.logger.info("[lib.gen.retro]    " + i + " = \"" + names[i] + "\"");
            }
        }

        CompoundTag data = nbt.getCompound("data");
        for (String key : data.getAllKeys()) {
            ChunkPos pos = deserializeChunkPos(key);
            if (pos == null) {
                continue;
            }
            byte[] genned = data.getByteArray(key);
            Set<String> actualGenned = new HashSet<>();
            for (byte i : genned) {
                if (i < 0 || i >= names.length) {
                    if (DEBUG) {
                        BCLog.logger.warn("[lib.gen.retro] Invalid chunk registry id " + i);
                    }
                    continue;
                }
                actualGenned.add(names[i]);
            }
        }
    }

    private static ChunkPos deserializeChunkPos(String key) {
        String[] parts = key.split(",");
        if (parts.length != 2) {
            if (DEBUG) {
                BCLog.logger.warn("[lib.gen.retro] Invalid chunk key " + key);
            }
            return null;
        }
        String x = parts[0];
        String z = parts[1];
        try {
            int cx = Integer.parseInt(x);
            int cz = Integer.parseInt(z);
            return new ChunkPos(cx, cz);
        } catch (NumberFormatException nfe) {
            if (DEBUG) {
                BCLog.logger.warn("[lib.gen.retro] Invalid chunk key " + key + " (" + nfe.getMessage() + ")");
            }
            return null;
        }
    }

    private static String serializeChunkPos(ChunkPos pos) {
        return pos.x + "," + pos.z;
    }

    @Override
//    public CompoundTag writeToNBT(CompoundTag nbt)
    public CompoundTag save(CompoundTag nbt) {
        Set<String> allNames = new HashSet<>();
        for (Set<String> used : gennedChunks.values()) {
            allNames.addAll(used);
        }
        Object2ByteOpenHashMap<String> map = new Object2ByteOpenHashMap<>();
        List<String> list = new ArrayList<>(allNames);
        ListTag registry = new ListTag();
        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i);
            map.put(name, (byte) i);
            registry.add(StringTag.valueOf(name));
        }
        nbt.put("registry", registry);

        CompoundTag data = new CompoundTag();
        for (Entry<ChunkPos, Set<String>> entry : gennedChunks.entrySet()) {
            String key = serializeChunkPos(entry.getKey());
            Set<String> names = entry.getValue();
            ByteArrayList ids = new ByteArrayList();
            for (String s : names) {
                byte b = map.get(s);
                ids.add(b);
            }
            data.putByteArray(key, ids.toByteArray());
        }
        nbt.put("data", data);

        return nbt;
    }
}
