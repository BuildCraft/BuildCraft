package buildcraft.core.list;

import java.util.Set;

import net.minecraft.item.ItemStack;

import buildcraft.api.lists.ListMatchHandler;

public class ListMatchHandlerTools extends ListMatchHandler {
	@Override
	public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
		if (type == Type.TYPE) {
			Set<String> toolClassesSource = stack.getItem().getToolClasses(stack);
			Set<String> toolClassesTarget = target.getItem().getToolClasses(stack);
			if (toolClassesSource.size() > 0 && toolClassesTarget.size() > 0) {
				if (precise) {
					if (toolClassesSource.size() != toolClassesTarget.size()) {
						return false;
					}
				}
				for (String s : toolClassesSource) {
					if (!toolClassesTarget.contains(s)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isValidSource(Type type, ItemStack stack) {
		return stack.getItem().getToolClasses(stack).size() > 0;
	}
}
