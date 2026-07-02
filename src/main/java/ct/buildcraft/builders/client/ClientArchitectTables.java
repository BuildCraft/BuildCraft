/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class ClientArchitectTables {
    public static final int START_BOX_VALUE = 3;
    public static final int START_SCANNED_BLOCK_VALUE = 50;
    public static final Object2IntMap<AABB> BOXES = new Object2IntOpenHashMap<>();
    public static final Object2IntMap<BlockPos> SCANNED_BLOCKS = new Object2IntOpenHashMap<>();

    public static void tick() {
        BOXES.replaceAll((k, v) -> v - 1);
        BOXES.values().removeIf(i -> i <= 0);
        SCANNED_BLOCKS.replaceAll((k, v) -> v - 1);
        SCANNED_BLOCKS.values().removeIf(i -> i <= 0);
    }
}
