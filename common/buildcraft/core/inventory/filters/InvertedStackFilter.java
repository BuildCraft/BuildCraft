package buildcraft.core.inventory.filters;

import net.minecraft.item.ItemStack;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class InvertedStackFilter implements IStackFilter {

	private final IStackFilter filter;

	public InvertedStackFilter(IStackFilter filter) {
		this.filter = filter;
	}

	@Override
	public boolean matches(ItemStack stack) {
		if (stack == null) {
			return false;
		}
		return !filter.matches(stack);
	}
}
