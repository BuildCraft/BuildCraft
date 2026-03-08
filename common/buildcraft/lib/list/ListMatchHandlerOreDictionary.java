/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import buildcraft.api.lists.ListMatchHandler;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

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
        ResourceLocation[] oreIds = ItemTags.getAllTags().getMatchingTags(stack.getItem()).toArray(new ResourceLocation[0]);

        if (oreIds.length == 0) {
            // Unfortunately we cannot compare the items.
            return false;
        }

//        int[] matchesIds = OreDictionary.getOreIDs(target);
        ResourceLocation[] matchesIds = ItemTags.getAllTags().getMatchingTags(target.getItem()).toArray(new ResourceLocation[0]);

//        String[] oreNames = new String[oreIds.length];
//        for (int i = 0; i < oreIds.length; i++) {
//            oreNames[i] = OreDictionary.getOreName(oreIds[i]);
//        }
        ResourceLocation[] oreNames = oreIds;

        if (type == Type.CLASS) {
//            for (int i : oreIds)
            for (ResourceLocation i : oreIds) {
//                for (int j : matchesIds)
                for (ResourceLocation j : matchesIds) {
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
//            INamedTag<Item> s = getBestOreString(oreNames);
//            if (s != null){
//                Set<Integer> stackIds = ListOreDictionaryCache.INSTANCE.getListOfPartialMatches(
//                        type == Type.MATERIAL ? ListOreDictionaryCache.getMaterial(s) : ListOreDictionaryCache.getType(s));
            ResourceLocation[] stackIds = oreNames;
//                if (stackIds != null) {
//                    for (int j : stackIds)
            for (ResourceLocation j : stackIds) {
//                        for (int k : matchesIds)
                for (ResourceLocation k : matchesIds) {
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
        return !ItemTags.getAllTags().getMatchingTags(stack.getItem()).isEmpty();
    }

    // private static String getBestOreString(String[] oreIds)
    private static ResourceLocation getBestOreString(ResourceLocation[] oreIds) {
//        String s = null;
        ResourceLocation s = null;
        int suc = 0, suct;
        for (ResourceLocation st : oreIds) {
//            suct = getUppercaseCount(st);
            suct = getUppercaseCount(st.toString());
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
        Set<ResourceLocation> oreIds = stack.getItem().getTags();
        NonNullList<ItemStack> stacks = NonNullList.create();

        if (oreIds.size() == 0) {
            // No ore IDs? Time for the best effort plan of METADATA!
            if (type == Type.TYPE) {
                NonNullList<ItemStack> tempStack = NonNullList.create();
//                stack.getItem().getSubItems(ItemGroup.SEARCH, tempStack);
                stack.getItem().fillItemCategory(ItemGroup.TAB_SEARCH, tempStack);
                for (ItemStack is : tempStack) {
                    if (is.getItem() == stack.getItem()) {
                        stacks.add(is);
                    }
                }
            }
            return stacks;
        }

//        String[] oreNames = new String[oreIds.length];
        Set<ResourceLocation> oreNames = oreIds;
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
        for (ResourceLocation j : oreNames) {
            stacks.addAll(ItemTags.getAllTags().getTag(j).getValues().stream().map(i -> new ItemStack(i, 1)).collect(Collectors.toList()));
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
////            is.getItem().getSubItems(ItemGroup.MISC, wll);
//            is.getItem().fillItemCategory(ItemGroup.TAB_MISC, wll);
//            wll.add(is);
//            if (wll.size() > 0) {
//                stacks.remove(is);
//                stacks.addAll(wll);
//            }
//        }

        return stacks;
    }
}
