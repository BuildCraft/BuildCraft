package buildcraft.core.list;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import buildcraft.api.lists.ListMatchHandler;

public class ListMatchHandlerTools extends ListMatchHandler {
	public static final Set<Class<? extends Item>> itemClasses = new HashSet<Class<? extends Item>>();

	@Override
	public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
		if (type == Type.TYPE) {
			Set<String> toolClassesSource = stack.getItem().getToolClasses(stack);
			Set<String> toolClassesTarget = target.getItem().getToolClasses(stack);
			if (toolClassesSource.size() > 0 && toolClassesTarget.size() > 0) {
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
