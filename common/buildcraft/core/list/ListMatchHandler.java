package buildcraft.core.list;

import java.util.List;
import net.minecraft.item.ItemStack;

/**
 * Internal interface for now - it will become public once its shape is set
 * in stone better.
 */
public abstract class ListMatchHandler {
	public enum Type {
		TYPE, MATERIAL, CLASS
	}

	public abstract boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise);
	public abstract List<ItemStack> getClientExamples(Type type, ItemStack stack);
}
