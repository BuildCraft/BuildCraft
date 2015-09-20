package buildcraft.core.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.core.lib.inventory.StackHelper;

public class ListMatchHandlerOreDictionary extends ListMatchHandler {
	private int getUppercaseCount(String s) {
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
				return StackHelper.isMatchingItem(stack, target, false, false);
			}
			return false;
		}

		int[] matchesIds = OreDictionary.getOreIDs(target);

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
			String s = getBestOreString(oreIds);
			if (s != null) {
				Set<Integer> stackIds = ListOreDictionaryCache.INSTANCE.getListOfPartialMatches(
						type == Type.MATERIAL ? ListOreDictionaryCache.getMaterial(s) : ListOreDictionaryCache.getType(s)
				);
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

	private String getBestOreString(int[] oreIds) {
		String s = null, st;
		int suc = 0, suct;
		for (int i : oreIds) {
			st = OreDictionary.getOreName(i);
			suct = getUppercaseCount(st);
			if (s == null || suct > suc) {
				s = st;
				suc = suct;
			}
		}
		return s;
	}

	@Override
	public List<ItemStack> getClientExamples(Type type, ItemStack stack) {
		int[] oreIds = OreDictionary.getOreIDs(stack);
		List<ItemStack> stacks = new ArrayList<ItemStack>();

		if (oreIds.length == 0) {
			// No ore IDs? Time for the best effort plan of METADATA!
			if (type == Type.TYPE) {
				List<ItemStack> tempStack = new ArrayList<ItemStack>();
				stack.getItem().getSubItems(stack.getItem(), CreativeTabs.tabMisc, tempStack);
				for (ItemStack is : tempStack) {
					if (is.getItem() == stack.getItem()) {
						stacks.add(is);
					}
				}
			}
			return stacks;
		}

		if (type == Type.CLASS) {
			for (int i : oreIds) {
				stacks.addAll(OreDictionary.getOres(i));
			}
		} else {
			String s = getBestOreString(oreIds);
			if (s != null) {
				Set<Integer> stackIds = ListOreDictionaryCache.INSTANCE.getListOfPartialMatches(
						type == Type.MATERIAL ? ListOreDictionaryCache.getMaterial(s) : ListOreDictionaryCache.getType(s)
				);
				if (stackIds != null) {
					for (int j : stackIds) {
						stacks.addAll(OreDictionary.getOres(j));
					}
				}
			}
		}

		List<ItemStack> wildcard = new ArrayList<ItemStack>();

		for (ItemStack is : stacks) {
			if (is != null && is.getItemDamage() == OreDictionary.WILDCARD_VALUE && is.getHasSubtypes()) {
				wildcard.add(is);
			}
		}
		for (ItemStack is : wildcard) {
			List<ItemStack> wll = new ArrayList<ItemStack>();
			is.getItem().getSubItems(is.getItem(), CreativeTabs.tabMisc, wll);
			if (wll.size() > 0) {
				stacks.remove(is);
				stacks.addAll(wll);
			}
		}

		return stacks;
	}
}
