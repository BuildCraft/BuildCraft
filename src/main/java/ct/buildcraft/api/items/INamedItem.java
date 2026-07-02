package ct.buildcraft.api.items;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

public interface INamedItem {
    String getLabelName(@Nonnull ItemStack stack);

    boolean setLabelName(@Nonnull ItemStack stack, String name);
}
