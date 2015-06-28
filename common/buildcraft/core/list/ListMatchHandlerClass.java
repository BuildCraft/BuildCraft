package buildcraft.core.list;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ListMatchHandlerClass extends ListMatchHandler {
	public static final Set<Class<? extends Item>> itemClasses = new HashSet<Class<? extends Item>>();

	@Override
	public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
		if (type == Type.TYPE) {
			Class kl = stack.getItem().getClass();
			return itemClasses.contains(kl) && kl.equals(target.getClass());
		}
		return false;
	}

	@Override
	public List<ItemStack> getClientExamples(Type type, ItemStack stack) {
		return null;
	}
}
