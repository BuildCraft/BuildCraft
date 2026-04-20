/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.list;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import ct.buildcraft.api.lists.ListMatchHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        Stream<TagKey<Item>> oreIds = stack.getTags().filter((tag) -> tag.location().getPath().startsWith("ores/"));
        Stream<TagKey<Item>> matchesIds = target.getTags();


        if (type == Type.CLASS) {
        	if(oreIds.anyMatch(target::is))
        		return true;
 /*       } else {
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
            }*/
        }

        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        return !stack.getTags().findFirst().isEmpty();
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public NonNullList<ItemStack> getClientExamples(Type type, @Nonnull ItemStack stack) {
        Stream<TagKey<Item>> oreIds = stack.getTags();
        NonNullList<ItemStack> stacks = NonNullList.create();

/*        if (oreIds.length == 0) {
            // No ore IDs? Time for the best effort plan of METADATA!
            if (type == Type.TYPE) {
                NonNullList<ItemStack> tempStack = NonNullList.create();
                stack.getItem().getSubItems(CreativeTabs.SEARCH, tempStack);
                for (ItemStack is : tempStack) {
                    if (is.getItem() == stack.getItem()) {
                        stacks.add(is);
                    }
                }
            }
            return stacks;
        }*/
/*
        String[] oreNames = new String[oreIds.length];
        for (int i = 0; i < oreIds.length; i++) {
            oreNames[i] = OreDictionary.getOreName(oreIds[i]);
        }

        if (type == Type.CLASS) {
            for (String s : oreNames) {
                stacks.addAll(OreDictionary.getOres(s));Blocks
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

        NonNullList<ItemStack> wildcard = NonNullList.create();

        for (ItemStack is : stacks) {
            if (is != null && is.getItemDamage() == OreDictionary.WILDCARD_VALUE && is.getHasSubtypes()) {
                wildcard.add(is);
            }
        }
        for (ItemStack is : wildcard) {
            NonNullList<ItemStack> wll = NonNullList.create();
            is.getItem().getSubItems(CreativeTabs.MISC, wll);
            if (wll.size() > 0) {
                stacks.remove(is);
                stacks.addAll(wll);
            }
        }
*///TODO
        return stacks;
    }
}
