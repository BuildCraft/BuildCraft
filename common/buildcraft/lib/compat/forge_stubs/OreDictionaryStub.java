/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.compat.forge_stubs;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * STUB(R.Chen): compile-only shim replacing {@code net.minecraftforge.oredict.OreDictionary}.
 *
 * OreDictionaryStub was removed in Fabric. This stub returns empty/no-op results so
 * unmigrated code compiles. All methods will return empty results until replaced
 * by the Tags system.
 *
 * TODO(R.Chen): replace all call sites with Fabric Tags lookups (data/tags/).
 */
public final class OreDictionaryStub {

    private OreDictionaryStub() {}

    /** Wildcard meta value (32767) used by Forge ore-dict matching. */
    public static final int WILDCARD_VALUE = 32767;

    public static void registerOre(String name, Object entry) {
        // no-op — TODO(R.Chen): register via Tags in data/
    }

    public static int[] getOreIDs(ItemStack stack) {
        return new int[0];
    }

    public static String getOreName(int id) {
        return "unknown";
    }

    public static int getOreID(String name) {
        return -1;
    }

    public static List<ItemStack> getOres(String name) {
        return Collections.emptyList();
    }

    public static String[] getOreNames() {
        return new String[0];
    }

    public static boolean containsMatch(boolean strict, ItemStack[] inputs, ItemStack... targets) {
        return false;
    }

    /** STUB(R.Chen): Forge OreDictionary.itemMatches — simplified to exact item match (ignores ore-dict). */
    public static boolean itemMatches(ItemStack target, ItemStack input, boolean strict) {
        if (target.isEmpty() || input.isEmpty()) return target.isEmpty() == input.isEmpty();
        return target.getItem() == input.getItem();
    }
}
