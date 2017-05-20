package buildcraft.lib.inventory;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IInjectable;

import javax.annotation.Nonnull;

public enum NoSpaceInjectable implements IInjectable {
    INSTANCE;

    @Override
    public boolean canInjectItems(EnumFacing from) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack injectItem(@Nonnull ItemStack stack, boolean doAdd, EnumFacing from, EnumDyeColor color, double speed) {
        return stack;
    }
}
