package buildcraft.core.list;

import net.minecraft.item.ItemStack;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.api.lists.ListRegistry;

public class ListMatchHandlerClass extends ListMatchHandler {
	@Override
	public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
		if (type == Type.TYPE) {
			Class kl = stack.getItem().getClass();
			return ListRegistry.itemClassAsType.contains(kl) && kl.equals(target.getClass());
		}
		return false;
	}

	@Override
	public boolean isValidSource(Type type, ItemStack stack) {
		if (type == Type.TYPE) {
			Class kl = stack.getItem().getClass();
			return ListRegistry.itemClassAsType.contains(kl);
		}
		return false;
	}
}
