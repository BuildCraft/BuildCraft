/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.reload;

import buildcraft.api.core.BCLog;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import java.util.*;

public enum ReloadManager {
    INSTANCE;

    public static final ReloadSource CONFIG_COLOUR_BLIND;
    public static final ReloadSource CONFIG_ROTATE_TRAVEL_ITEMS;

    private final Multimap<ReloadSource, Reloadable> dependencies = HashMultimap.create();
    private final Multimap<Reloadable, ReloadSource> parents = HashMultimap.create();

    public void addDependency(ReloadSource from, IReloadable to, ReloadSource locationTo) {
        Reloadable reloadable = new Reloadable(to, locationTo);
        dependencies.put(from, reloadable);
        parents.put(reloadable, from);
    }

    public void addDependency(ReloadSource from, Runnable to) {
        addDependency(from, (sources) ->
        {
            to.run();
            return false;
        }, null);
    }

    public void addDependency(ReloadSource from, ReloadSource to) {
        addDependency(from, null, to);
    }

    public void preReloadResources() {
        dependencies.clear();
        parents.clear();
    }

    public void postReload(ReloadSource location) {
        postReload(ImmutableList.of(location));
    }

    /** Call this *after* the given {@link ReloadSource} has been reloaded. */
    public void postReload(Collection<ReloadSource> locations) {
        Set<ReloadSource> allReloadable = new HashSet<>();
        /* Populate allReloadable with ALL possible ReloadSource's that might be reloaded during this call */
        {
            List<ReloadSource> toVisit = new LinkedList<>(locations);
            while (!toVisit.isEmpty()) {
                Iterator<ReloadSource> itr = toVisit.iterator();
                ReloadSource s = itr.next();
                itr.remove();
                if (allReloadable.add(s)) {
                    for (Reloadable r : dependencies.get(s)) {
                        if (r.source != null) {
                            toVisit.add(r.source);
                        }
                    }
                }
            }
        }
        Set<ReloadSource> reloaded = new HashSet<>(locations);
        List<Reloadable> toReload = new ArrayList<>();
        Set<Reloadable> potentialReloadableSet = new HashSet<>();
        for (ReloadSource loc : locations) {
            potentialReloadableSet.addAll(dependencies.get(loc));
        }
        List<Reloadable> potentialReloadables = new LinkedList<>(potentialReloadableSet);
        boolean hasChanged;
        do {
            hasChanged = false;
            Iterator<Reloadable> potentialItr = potentialReloadables.iterator();
            while (potentialItr.hasNext()) {
                Reloadable r = potentialItr.next();
                searchForNonReloadedParent:
                {
                    for (ReloadSource parent : parents.get(r)) {
                        if (allReloadable.contains(parent)) {
                            if (!reloaded.contains(parent)) {
                                break searchForNonReloadedParent;
                            }
                        }
                    }
                    potentialItr.remove();
                    toReload.add(r);
                }
            }
            for (Reloadable r : toReload) {
                boolean addChildren;
                if (r.reloadable == null) {
                    addChildren = true;
                } else {
                    addChildren = r.reloadable.reload(allReloadable);
                }
                if (r.source != null) {
                    reloaded.add(r.source);
                    if (addChildren) {
                        potentialReloadables.addAll(dependencies.get(r.source));
                        hasChanged = true;
                    }
                }
            }
            toReload.clear();
        }
        while (hasChanged);
        if (potentialReloadables.isEmpty()) {
            return;
        }
        BCLog.logger.fatal("Detected a cyclic dependency chain!");
        BCLog.logger.fatal("Reloadables involved:");
        for (Reloadable r : toReload) {
            BCLog.logger.fatal("  - " + r);
        }
        throw new IllegalStateException("Cyclic dependency chain!");
    }

    private static final class Reloadable {
        final IReloadable reloadable;
        final ReloadSource source;
        final int hash;

        public Reloadable(IReloadable reloadable, ReloadSource source) {
            this.reloadable = reloadable;
            this.source = source;
            this.hash = Objects.hash(reloadable, source);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null) return false;
            if (obj.getClass() != getClass()) {
                return false;
            }
            Reloadable other = (Reloadable) obj;
            return reloadable == other.reloadable//
                    && Objects.equals(source, other.source);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "Reloadable [ " + source + " - " + reloadable + "  ]";
        }
    }

    static {
        // 1.18.2: forced lower
//        CONFIG_COLOUR_BLIND = cfg("config/colourBlindMode");
//        CONFIG_ROTATE_TRAVEL_ITEMS = cfg("config/rotateTravelingItems");
        CONFIG_COLOUR_BLIND = cfg("config/colour_blind_mode");
        CONFIG_ROTATE_TRAVEL_ITEMS = cfg("config/rotate_traveling_items");
    }

    private static ReloadSource cfg(String path) {
        return new ReloadSource("buildcraftlib", path, SourceType.CONFIG);
    }
}
