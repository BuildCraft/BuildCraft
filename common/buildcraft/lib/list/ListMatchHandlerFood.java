package buildcraft.lib.list;

import buildcraft.api.lists.ListMatchHandler;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

// Calen
public class ListMatchHandlerFood extends ListMatchHandler {
    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            return stack.getItem().isEdible() && target.getItem().isEdible();
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        return stack.getItem().isEdible();
    }
}
