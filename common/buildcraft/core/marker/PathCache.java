/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.marker;

import buildcraft.lib.marker.MarkerCache;
import net.minecraft.world.level.Level;

public class PathCache extends MarkerCache<PathSubCache> {
    public static final PathCache INSTANCE = new PathCache();

    public PathCache() {
        super("path");
    }

    @Override
    protected PathSubCache createSubCache(Level world) {
        return new PathSubCache(world);
    }
}
