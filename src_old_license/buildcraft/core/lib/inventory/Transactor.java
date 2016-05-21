/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

public abstract class Transactor implements ITransactor {
    @Override
    public ItemStack insert(ItemStack stack, boolean doAdd) {
        ItemStack added = stack.copy();
        added.stackSize = inject(stack, doAdd);
        return added;
    }

    /**
     * 
     * @return The number of items that were injected
     */
    public abstract int inject(ItemStack stack, boolean doAdd);

    public static ITransactor getTransactorFor(Object object, EnumFacing orientation) {
        IItemHandler handler = InvUtils.getItemHandler(object, orientation);
        if (handler instanceof ITransactor) {
            return (ITransactor) handler;
        }
        return handler != null ? new TransactorItemHandler(handler) : null;
    }
}
