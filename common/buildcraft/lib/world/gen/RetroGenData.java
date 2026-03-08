/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.world.gen;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.map.hash.TObjectByteHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.Map.Entry;

//public class RetroGenData extends WorldSavedData
public class RetroGenData extends WorldSavedData {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.gen.retro");
    public static final String NAME = "buildcraft_world_gen";
    private final Map<ChunkPos, Set<String>> gennedChunks = new HashMap<>();

    public RetroGenData() {
        this(NAME);
    }

    public RetroGenData(String name) {
        super(name);
    }

    @Override
    public void load(CompoundNBT nbt) {
        gennedChunks.clear();

        ListNBT registry = nbt.getList("registry", Constants.NBT.TAG_STRING);
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

        CompoundNBT data = nbt.getCompound("data");
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
//    public CompoundNBT writeToNBT(CompoundNBT nbt)
    public CompoundNBT save(CompoundNBT nbt) {
        Set<String> allNames = new HashSet<>();
        for (Set<String> used : gennedChunks.values()) {
            allNames.addAll(used);
        }
        TObjectByteHashMap<String> map = new TObjectByteHashMap<>();
        List<String> list = new ArrayList<>(allNames);
        ListNBT registry = new ListNBT();
        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i);
            map.put(name, (byte) i);
            registry.add(StringNBT.valueOf(name));
        }
        nbt.put("registry", registry);

        CompoundNBT data = new CompoundNBT();
        for (Entry<ChunkPos, Set<String>> entry : gennedChunks.entrySet()) {
            String key = serializeChunkPos(entry.getKey());
            Set<String> names = entry.getValue();
            TByteArrayList ids = new TByteArrayList();
            for (String s : names) {
                byte b = map.get(s);
                ids.add(b);
            }
            data.putByteArray(key, ids.toArray());
        }
        nbt.put("data", data);

        return nbt;
    }
}
