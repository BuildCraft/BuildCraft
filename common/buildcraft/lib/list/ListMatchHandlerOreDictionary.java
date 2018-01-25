/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

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
        int[] oreIds = OreDictionary.getOreIDs(stack);

        if (oreIds.length == 0) {
            // Unfortunately we cannot compare the items.
            return false;
        }

        int[] matchesIds = OreDictionary.getOreIDs(target);

        String[] oreNames = new String[oreIds.length];
        for (int i = 0; i < oreIds.length; i++) {
            oreNames[i] = OreDictionary.getOreName(oreIds[i]);
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
            // Always pick only the longest OreDictionary string for matching.
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
        return OreDictionary.getOreIDs(stack).length > 0;
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

    @SideOnly(Side.CLIENT)
    @Override
    public List<ItemStack> getClientExamples(Type type, @Nonnull ItemStack stack) {
        int[] oreIds = OreDictionary.getOreIDs(stack);
        List<ItemStack> stacks = Lists.newArrayList();

        if (oreIds.length == 0) {
            // No ore IDs? Time for the best effort plan of METADATA!
            if (type == Type.TYPE) {
                List<ItemStack> tempStack = Lists.newArrayList();
                stack.getItem().getSubItems(stack.getItem(), CreativeTabs.SEARCH, tempStack);
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
            oreNames[i] = OreDictionary.getOreName(oreIds[i]);
        }

        if (type == Type.CLASS) {
            for (String s : oreNames) {
                stacks.addAll(OreDictionary.getOres(s));
            }
        } else {
            String s = getBestOreString(oreNames);
            if (s != null) {
                Set<Integer> stackIds = ListOreDictionaryCache.INSTANCE.getListOfPartialMatches(
                        type == Type.MATERIAL ? ListOreDictionaryCache.getMaterial(s) : ListOreDictionaryCache.getType(s));
                if (stackIds != null) {
                    for (int j : stackIds) {
                        stacks.addAll(OreDictionary.getOres(OreDictionary.getOreName(j)));
                    }
                }
            }
        }

        List<ItemStack> wildcard = Lists.newArrayList();

        for (ItemStack is : stacks) {
            if (is != null && is.getItemDamage() == OreDictionary.WILDCARD_VALUE && is.getHasSubtypes()) {
                wildcard.add(is);
            }
        }
        for (ItemStack is : wildcard) {
            List<ItemStack> wll = Lists.newArrayList();
            is.getItem().getSubItems(is.getItem(), CreativeTabs.MISC, wll);
            if (wll.size() > 0) {
                stacks.remove(is);
                stacks.addAll(wll);
            }
        }

        return stacks;
    }
}
