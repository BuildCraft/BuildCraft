package buildcraft.core.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.core.lib.utils.OreDictionaryCache;

public class ListMatchHandlerOreDictionary extends ListMatchHandler {
	@Override
	public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
		int[] oreIds = OreDictionary.getOreIDs(stack);
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
			for (int i : oreIds) {
				String s = OreDictionary.getOreName(i);
				Set<Integer> stackIds = OreDictionaryCache.INSTANCE.getListOfPartialMatches(
						type == Type.MATERIAL ? OreDictionaryCache.getSecondHalf(s) : OreDictionaryCache.getFirstHalf(s)
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
	public List<ItemStack> getClientExamples(Type type, ItemStack stack) {
		int[] oreIds = OreDictionary.getOreIDs(stack);
		List<ItemStack> stacks = new ArrayList<ItemStack>();

		if (type == Type.CLASS) {
			for (int i : oreIds) {
				stacks.addAll(OreDictionary.getOres(i));
			}
		} else {
			for (int i : oreIds) {
				String s = OreDictionary.getOreName(i);
				Set<Integer> stackIds = OreDictionaryCache.INSTANCE.getListOfPartialMatches(
						type == Type.MATERIAL ? OreDictionaryCache.getSecondHalf(s) : OreDictionaryCache.getFirstHalf(s)
				);
				if (stackIds != null) {
					for (int j : stackIds) {
						stacks.addAll(OreDictionary.getOres(j));
					}
				}
			}
		}

		return stacks;
	}
}
