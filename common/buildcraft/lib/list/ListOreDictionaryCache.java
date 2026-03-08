/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;


import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public final class ListOreDictionaryCache {
    public static final ListOreDictionaryCache INSTANCE = new ListOreDictionaryCache();
    private static final String[] TYPE_KEYWORDS = { "Tiny", "Dense", "Small" };
    // Calen not still used in 1.18
//    private final Map<String, Set<Integer>> namingCache = new HashMap<>();
//    private final Set<String> registeredNames = new HashSet<>();
    private final Set<ResourceLocation> registeredNames = new HashSet<>();

    private ListOreDictionaryCache() {

    }

    // Calen not still used in 1.18
//    public Set<Integer> getListOfPartialMatches(String part) {
//        return namingCache.get(part);
//    }

    // Calen not still used in 1.18
//    private void addToNamingCache(String s, int id) {
//        if (s == null) {
//            return;
//        }
//
//        Set<Integer> ll = namingCache.get(s);
//
//        if (ll == null) {
//            ll = new HashSet<>();
//            ll.add(id);
//            namingCache.put(s, ll);
//        } else {
//            ll.add(id);
//        }
//    }

    public static String getType(String name) {
        // Rules for finding type:
        // - Split just before the last uppercase character found.
        int splitLocation = name.length() - 1;
        while (splitLocation >= 0) {
            if (Character.isUpperCase(name.codePointAt(splitLocation))) {
                break;
            } else {
                splitLocation--;
            }
        }
        return splitLocation >= 0 ? name.substring(0, splitLocation) : name; // No null - this handles things like
        // "record".
    }

    public static String getMaterial(String name) {
        // Rules for finding material:
        // - For every uppercase character, check if the character is not in
        // TYPE_KEYWORDS. This is used to skip things like "plate[DenseIron]"
        // or "dust[TinyRedstone]". That part should be the material still.
        int splitLocation = 0;
        String t = null;
        while (splitLocation < name.length()) {
            if (!Character.isUpperCase(name.codePointAt(splitLocation))) {
                splitLocation++;
            } else {
                t = name.substring(splitLocation);
                for (String s : TYPE_KEYWORDS) {
                    if (t.startsWith(s)) {
                        t = null;
                        break;
                    }
                }
                if (t != null) {
                    break;
                } else {
                    splitLocation++;
                }
            }
        }
        return splitLocation < name.length() ? t : null;
    }

    // public void registerName(String name)
    public void registerName(ResourceLocation name) {
        if (registeredNames.contains(name)) {
            return;
        }

        // Calen not still used in 1.18
//        int oreID = OreDictionary.getOreID(name);
//
//        addToNamingCache(getType(name), oreID);
//        addToNamingCache(getMaterial(name), oreID);

        registeredNames.add(name);
    }
}
