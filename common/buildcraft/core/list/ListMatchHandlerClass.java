package buildcraft.core.list;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.creativetab.CreativeTabs;
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
		if (type == Type.TYPE) {
			Class kl = stack.getItem().getClass();
			List<ItemStack> examples = new ArrayList<ItemStack>();
			if (itemClasses.contains(kl)) {
				for (Object key : Item.itemRegistry.getKeys()) {
					Item i = (Item) Item.itemRegistry.getObject(key);
					if (i != null && kl.equals(i.getClass())) {
						i.getSubItems(i, CreativeTabs.tabMisc, examples);
					}
				}
			}
			return examples;
		}
		return null;
	}
}
