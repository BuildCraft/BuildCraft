/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.lib.misc.ItemUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

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
//        int[] oreIds = OreDictionary.getOreIDs(stack);
        TagKey<Item>[] oreIds = stack.getTags().toList().toArray(new TagKey[0]);

        if (oreIds.length == 0) {
            // Unfortunately we cannot compare the items.
            return false;
        }

//        int[] matchesIds = OreDictionary.getOreIDs(target);
        TagKey<Item>[] matchesIds = target.getTags().toList().toArray(new TagKey[0]);

//        String[] oreNames = new String[oreIds.length];
//        for (int i = 0; i < oreIds.length; i++) {
//            oreNames[i] = OreDictionary.getOreName(oreIds[i]);
//        }
        TagKey<Item>[] oreNames = oreIds;

        if (type == Type.CLASS) {
//            for (int i : oreIds)
            for (TagKey<Item> i : oreIds) {
//                for (int j : matchesIds)
                for (TagKey<Item> j : matchesIds) {
//                    if (i == j)
                    if (i.equals(j)) {
                        return true;
                    }
                }
            }
        } else {
            // Always pick only the longest OreDictionary string for matching.
            // It's ugly, but should give us the most precise result for the
            // cases in which a given stone is also used for crafting equivalents.
//            String s = getBestOreString(oreNames);
//            TagKey<Item> s = getBestOreString(oreNames);
//            if (s != null){
//                Set<Integer> stackIds = ListOreDictionaryCache.INSTANCE.getListOfPartialMatches(
//                        type == Type.MATERIAL ? ListOreDictionaryCache.getMaterial(s) : ListOreDictionaryCache.getType(s));
            TagKey<Item>[] stackIds = oreNames;
//                if (stackIds != null) {
//                    for (int j : stackIds)
            for (TagKey<Item> j : stackIds) {
//                        for (int k : matchesIds)
                for (TagKey<Item> k : matchesIds) {
//                    if (j == k)
                    if (j.equals(k)) {
                        return true;
                    }
                }
//                }
            }
//            }
        }

        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
//        return OreDictionary.getOreIDs(stack).length > 0;
        return !stack.getTags().toList().isEmpty();
    }

    // private static String getBestOreString(String[] oreIds)
    private static TagKey<Item> getBestOreString(TagKey<Item>[] oreIds) {
//        String s = null;
        TagKey<Item> s = null;
        int suc = 0, suct;
        for (TagKey<Item> st : oreIds) {
//            suct = getUppercaseCount(st);
            suct = getUppercaseCount(st.location().toString());
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
//        int[] oreIds = OreDictionary.getOreIDs(stack);
        TagKey<Item>[] oreIds = stack.getTags().toArray(TagKey[]::new);
        NonNullList<ItemStack> stacks = NonNullList.create();

        if (oreIds.length == 0) {
            // No ore IDs? Time for the best effort plan of METADATA!
            if (type == Type.TYPE) {
                NonNullList<ItemStack> tempStack = NonNullList.create();
//                stack.getItem().getSubItems(CreativeModeTab.SEARCH, tempStack);
                ItemUtil.fillItemCategory(stack.getItem(), CreativeModeTabs.SEARCH, tempStack);
                for (ItemStack is : tempStack) {
                    if (is.getItem() == stack.getItem()) {
                        stacks.add(is);
                    }
                }
            }
            return stacks;
        }

//        String[] oreNames = new String[oreIds.length];
        TagKey<Item>[] oreNames = oreIds;
//        for (int i = 0; i < oreIds.length; i++) {
//            oreNames[i] = OreDictionary.getOreName(oreIds[i]);
//        }

//        if (type == Type.CLASS) {
//            for (String s : oreNames) {
//                stacks.addAll(OreDictionary.getOres(s));
//            }
//        } else {
//            String s = getBestOreString(oreNames);
//            if (s != null) {
//                Set<Integer> stackIds = ListOreDictionaryCache.INSTANCE.getListOfPartialMatches(
//                        type == Type.MATERIAL ? ListOreDictionaryCache.getMaterial(s) : ListOreDictionaryCache.getType(s));
//                if (stackIds != null) {
//                    for (int j : stackIds) {
//                        stacks.addAll(OreDictionary.getOres(OreDictionary.getOreName(j)));
//                    }
//                }
//            }
//        }
        for (TagKey<Item> j : oreNames) {
            stacks.addAll(ForgeRegistries.ITEMS.tags().getTag(j).stream().map(i -> new ItemStack(i, 1)).toList());
        }

//        NonNullList<ItemStack> wildcard = NonNullList.create();

        // Calen: no getHasSubtypes
//        for (ItemStack is : stacks) {
//            if (is != null && is.getItemDamage() == OreDictionary.WILDCARD_VALUE && is.getHasSubtypes()) {
//                wildcard.add(is);
//            }
//        }
//        for (ItemStack is : wildcard) {
//            NonNullList<ItemStack> wll = NonNullList.create();
////            is.getItem().getSubItems(CreativeModeTab.MISC, wll);
//            is.getItem().fillItemCategory(CreativeModeTab.TAB_MISC, wll);
//            wll.add(is);
//            if (wll.size() > 0) {
//                stacks.remove(is);
//                stacks.addAll(wll);
//            }
//        }

        return stacks;
    }
}
