package buildcraft.lib.inventory;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IInjectable;

public enum NoSpaceInjectable implements IInjectable {
    INSTANCE;

    @Override
    public boolean canInjectItems(EnumFacing from) {
        return false;
    }

    @Override
    public ItemStack injectItem(ItemStack stack, boolean doAdd, EnumFacing from, EnumDyeColor color, double speed) {
        return stack;
    }
}
