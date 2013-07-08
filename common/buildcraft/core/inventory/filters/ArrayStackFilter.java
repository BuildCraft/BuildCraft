package buildcraft.core.inventory.filters;

import net.minecraft.item.ItemStack;
import buildcraft.core.inventory.StackHelper;

/**
 * Returns true if the stack matches any one one of the filter stacks.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ArrayStackFilter implements IStackFilter {

	private final ItemStack[] stacks;

	public ArrayStackFilter(ItemStack... stacks) {
		this.stacks = stacks;
	}

	@Override
	public boolean matches(ItemStack stack) {
		if (stacks.length == 0 || !hasFilter()) {
			return true;
		}
		for (ItemStack s : stacks) {
			if (StackHelper.instance().isMatchingItem(s, stack)) {
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
