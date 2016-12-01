package buildcraft.lib.list;

import java.util.ArrayList;
import java.util.Set;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.lists.ListMatchHandler;

import buildcraft.lib.misc.StackUtil;

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
    public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
        int[] oreIds = OreDictionary.getOreIDs(stack);

        if (oreIds.length == 0) {
            // No ore IDs? Time for the best effort plan of METADATA!
            if (type == Type.TYPE) {
                return StackUtil.isMatchingItem(stack, target, false, false);
            }
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
                Set<Integer> stackIds = ListOreDictionaryCache.INSTANCE.getListOfPartialMatches(type == Type.MATERIAL ? ListOreDictionaryCache.getMaterial(s) : ListOreDictionaryCache.getType(s));
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
    public boolean isValidSource(Type type, ItemStack stack) {
        if (OreDictionary.getOreIDs(stack).length > 0) {
            return true;
        }
        if (type == Type.TYPE && stack.getHasSubtypes()) {
            return true;
        }
        return false;
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

    @Override
    public NonNullList<ItemStack> getClientExamples(Type type, ItemStack stack) {
        int[] oreIds = OreDictionary.getOreIDs(stack);
        NonNullList<ItemStack> stacks = new ArrayList<>();

        if (oreIds.length == 0) {
            // No ore IDs? Time for the best effort plan of METADATA!
            if (type == Type.TYPE) {
                NonNullList<ItemStack> tempStack = NonNullList.create();
                stack.getItem().getSubItems(stack.getItem(), CreativeTabs.MISC, tempStack);
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
                Set<Integer> stackIds = ListOreDictionaryCache.INSTANCE.getListOfPartialMatches(type == Type.MATERIAL ? ListOreDictionaryCache.getMaterial(s) : ListOreDictionaryCache.getType(s));
                if (stackIds != null) {
                    for (int j : stackIds) {
                        stacks.addAll(OreDictionary.getOres(OreDictionary.getOreName(j)));
                    }
                }
            }
        }

        NonNullList<ItemStack> wildcard = new ArrayList<>();

        for (ItemStack is : stacks) {
            if (is != null && is.getItemDamage() == OreDictionary.WILDCARD_VALUE && is.getHasSubtypes()) {
                wildcard.add(is);
            }
        }
        for (ItemStack is : wildcard) {
            NonNullList<ItemStack> wll = NonNullList.create();
            is.getItem().getSubItems(is.getItem(), CreativeTabs.MISC, wll);
            if (wll.size() > 0) {
                stacks.remove(is);
                stacks.addAll(wll);
            }
        }

        return stacks;
    }
}
