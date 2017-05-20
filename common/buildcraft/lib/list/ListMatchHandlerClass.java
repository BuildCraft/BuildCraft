package buildcraft.lib.list;

import net.minecraft.item.ItemStack;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.api.lists.ListRegistry;

import javax.annotation.Nonnull;

public class ListMatchHandlerClass extends ListMatchHandler {
    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            Class<?> kl = stack.getItem().getClass();
            return ListRegistry.itemClassAsType.contains(kl) && kl.equals(target.getClass());
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        if (type == Type.TYPE) {
            Class<?> kl = stack.getItem().getClass();
            return ListRegistry.itemClassAsType.contains(kl);
        }
        return false;
    }
}
