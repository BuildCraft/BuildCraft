package ct.buildcraft.api.items;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

public interface IList extends INamedItem {
    boolean matches(@Nonnull ItemStack stackList, @Nonnull ItemStack item);
}
