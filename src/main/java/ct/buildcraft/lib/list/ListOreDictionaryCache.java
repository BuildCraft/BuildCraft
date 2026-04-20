/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.list;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ListOreDictionaryCache {
    public static final ListOreDictionaryCache INSTANCE = new ListOreDictionaryCache();
    private static final String[] TYPE_KEYWORDS = { "Tiny", "Dense", "Small" };
    private final Map<String, Set<Integer>> namingCache = new HashMap<>();
//    private final Set<String> registeredNames = new HashSet<>();

    private ListOreDictionaryCache() {

    }

    public Set<Integer> getListOfPartialMatches(String part) {
        return namingCache.get(part);
    }

/*    private void addToNamingCache(String s, int id) {
        if (s == null) {
            return;
        }

        Set<Integer> ll = namingCache.get(s);

        if (ll == null) {
            ll = new HashSet<>();
            ll.add(id);
            namingCache.put(s, ll);
        } else {
            ll.add(id);
        }
    }*/
    
    private static String[] splitTags(String name) {
    	if(name.contains("\\")) {
    		String[] split = name.split("\\");
    		return split;
    	}
    	return null;
	}

    public static String getType(String name) {
    	String[] splitTags = splitTags(name);
    	if(splitTags == null)
    		return name;// No null - this handles things like
        				// "record".
    	return splitTags[0];
    }

    public static String getMaterial(String name) {
        // TYPE_KEYWORDS. This is used to skip things like "plate[DenseIron]"
        // or "dust[TinyRedstone]". That part should be the material still.
    	String[] splitTags = splitTags(name);
    	if(splitTags == null)
    		return name;// No null - this handles things like
        				// "record".
        String t = splitTags[0];
        for (String s : TYPE_KEYWORDS) {
            if (t.startsWith(s)) {
                t = null;
                break;
            }
        }
        return t;
    }

 /*   public void registerName(String name) {
        if (registeredNames.contains(name)) {
            return;
        }

        int oreID = TagKey.getOreID(name);

        addToNamingCache(getType(name), oreID);
        addToNamingCache(getMaterial(name), oreID);

        registeredNames.add(name);
    }*/
}
