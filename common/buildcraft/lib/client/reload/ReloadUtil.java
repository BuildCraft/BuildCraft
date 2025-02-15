/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.reload;

import net.minecraft.resources.ResourceLocation;

import java.util.EnumSet;
import java.util.Set;

public class ReloadUtil {
    /** Searches the given set for all of the {@link SourceType}'s that have been reloaded as the specified location
     * key.
     *
     * @param set The set to look in
     * @param location The identifier to search for.
     * @return An {@link EnumSet} of all of the {@link SourceType}'s that area contained in the set. */
    public static EnumSet<SourceType> getSourceTypesFor(Set<ReloadSource> set, ResourceLocation location) {
        EnumSet<SourceType> enumSet = EnumSet.noneOf(SourceType.class);
        /* This constant is magic atm - no perf data to back it up (feel free to change this later if this doesn't is a
         * performance issue. */
        if (set.size() > 10) {
            /* If its a hash-based set then this is *probably* faster than searching through the entire set. Hopefully
             * no-one passes a list based set into this... */
            for (SourceType type : SourceType.VALUES) {
                if (set.contains(new ReloadSource(location, type))) {
                    enumSet.add(type);
                }
            }
        } else {
            for (ReloadSource src : set) {
                if (src.location.equals(location)) {
                    enumSet.add(src.type);
                }
            }
        }
        return enumSet;
    }
}
