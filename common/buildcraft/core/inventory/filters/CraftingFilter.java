package buildcraft.core.inventory.filters;

import buildcraft.core.inventory.StackHelper;
import net.minecraft.item.ItemStack;

/**
 * Returns true if the stack matches any one one of the filter stacks.
 * Checks the OreDictionary and wildcards.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class CraftingFilter implements IStackFilter {

	private final ItemStack[] stacks;

	public CraftingFilter(ItemStack... stacks) {
		this.stacks = stacks;
	}

	@Override
	public boolean matches(ItemStack stack) {
		if (stacks.length == 0 || !hasFilter()) {
			return true;
		}
		for (ItemStack s : stacks) {
			if (StackHelper.instance().isCraftingEquivalent(s, stack, true)) {
				return true;
			}
		}
		return false;
	}

	public ItemStack[] getStacks() {
		return stacks;
	}

	public boolean hasFilter() {
		for (ItemStack filter : stacks) {
			if (filter != null) {
				return true;
			}
		}
		return false;
	}
}
