package buildcraft.core.blueprints;

import net.minecraft.item.ItemStack;

public class RequirementItemStack {
	public final ItemStack stack;
	public final int size;

	public RequirementItemStack(ItemStack stack, int size) {
		this.stack = stack;
		this.size = size;
		stack.stackSize = 1;
	}

	@Override
	public int hashCode() {
		return this.stack.hashCode() * 13 + this.size;
	}
}
