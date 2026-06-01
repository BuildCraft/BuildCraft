/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import java.util.Set;
import java.util.List;
import buildcraft.lib.compat.forge_stubs.OreDictionaryStub;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
// STUB(R.Chen): OreDictionaryStub removed — TODO(R.Chen): implement via Tags

import buildcraft.api.lists.ListMatchHandler;

public class ListMatchHandlerOreDictionary extends ListMatchHandler {
    private static int getUppercaseCount(String s) {
        int j = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.codePointAt(i))) {
                j++;
            }
        }
        return j;
    }

    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        int[] oreIds = OreDictionaryStub.getOreIDs(stack);

        if (oreIds.length == 0) {
            // Unfortunately we cannot compare the items.
            return false;
        }

        int[] matchesIds = OreDictionaryStub.getOreIDs(target);

        String[] oreNames = new String[oreIds.length];
        for (int i = 0; i < oreIds.length; i++) {
            oreNames[i] = OreDictionaryStub.getOreName(oreIds[i]);
        }

        if (type == Type.CLASS) {
            for (int i : oreIds) {
                for (int j : matchesIds) {
                    if (i == j) {
                        return true;
                    }
                }
            }
        } else {
            // Always pick only the longest OreDictionaryStub string for matching.
            // It's ugly, but should give us the most precise result for the
            // cases in which a given stone is also used for crafting equivalents.
            String s = getBestOreString(oreNames);
            if (s != null) {
                Set<Integer> stackIds = ListOreDictionaryCache.INSTANCE.getListOfPartialMatches(
                    type == Type.MATERIAL ? ListOreDictionaryCache.getMaterial(s) : ListOreDictionaryCache.getType(s));
                if (stackIds != null) {
                    for (int j : stackIds) {
                        for (int k : matchesIds) {
                            if (j == k) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        return OreDictionaryStub.getOreIDs(stack).length > 0;
    }

    private static String getBestOreString(String[] oreIds) {
        String s = null;
        int suc = 0, suct;
        for (String st : oreIds) {
            suct = getUppercaseCount(st);
            if (s == null || suct > suc) {
                s = st;
                suc = suct;
            }
        }
        return s;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public DefaultedList<ItemStack> getClientExamples(Type type, @Nonnull ItemStack stack) {
        int[] oreIds = OreDictionaryStub.getOreIDs(stack);
        DefaultedList<ItemStack> stacks = DefaultedList.of();

        if (oreIds.length == 0) {
            // No ore IDs? Time for the best effort plan of METADATA!
            if (type == Type.TYPE) {
                DefaultedList<ItemStack> tempStack = DefaultedList.of();
                stack.getItem().getSubItems(ItemGroup.SEARCH, tempStack);
                for (ItemStack is : tempStack) {
                    if (is.getItem() == stack.getItem()) {
                        stacks.add(is);
                    }
                }
            }
            return stacks;
        }

        String[] oreNames = new String[oreIds.length];
        for (int i = 0; i < oreIds.length; i++) {
            oreNames[i] = OreDictionaryStub.getOreName(oreIds[i]);
        }

        if (type == Type.CLASS) {
            for (String s : oreNames) {
                stacks.addAll(OreDictionaryStub.getOres(s));
            }
        } else {
            String s = getBestOreString(oreNames);
            if (s != null) {
                Set<Integer> stackIds = ListOreDictionaryCache.INSTANCE.getListOfPartialMatches(
                    type == Type.MATERIAL ? ListOreDictionaryCache.getMaterial(s) : ListOreDictionaryCache.getType(s));
                if (stackIds != null) {
                    for (int j : stackIds) {
                        stacks.addAll(OreDictionaryStub.getOres(OreDictionaryStub.getOreName(j)));
                    }
                }
            }
        }

        DefaultedList<ItemStack> wildcard = DefaultedList.of();

        for (ItemStack is : stacks) {
            if (is != null && is.getDamage() == OreDictionaryStub.WILDCARD_VALUE && is.getHasSubtypes()) {
                wildcard.add(is);
            }
        }
        for (ItemStack is : wildcard) {
            DefaultedList<ItemStack> wll = DefaultedList.of();
            is.getItem().getSubItems(ItemGroup.MISC, wll);
            if (wll.size() > 0) {
                stacks.remove(is);
                stacks.addAll(wll);
            }
        }

        return stacks;
    }
}
