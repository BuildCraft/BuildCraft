/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.dimension;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.BlockPos;

import buildcraft.builders.snapshot.Blueprint;

public class BlueprintLocationManager {
    public static final List<BlueprintLocation> locations = new ArrayList<>();

    public static BlueprintLocation getLocationFor(Blueprint blueprint) {
        int start = 0;
        int length = blueprint.size.getX();
        for(BlueprintLocation location: locations) {
            if (start + length > location.startPos.getX()) {
                start += location.size.getX();
            }
        }
        //add to the size to prevent 2 blueprints from touching and affecting each other
        return new BlueprintLocation(new BlockPos(start, 0, 0), blueprint.size.add(16, 0, 16));
    }

    public static void releaseLocation(BlueprintLocation location) {
        locations.remove(location);
    }

    public static class BlueprintLocation {
        public final BlockPos startPos, size;

        public BlueprintLocation(BlockPos startPos, BlockPos size) {
            this.startPos = startPos;
            this.size = size;
        }
    }
}
