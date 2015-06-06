/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public abstract class Transactor implements ITransactor {

    @Override
    public ItemStack add(ItemStack stack, EnumFacing orientation, boolean doAdd) {
        ItemStack added = stack.copy();
        added.stackSize = inject(stack, orientation, doAdd);
        return added;
    }

    public abstract int inject(ItemStack stack, EnumFacing orientation, boolean doAdd);

    public static ITransactor getTransactorFor(Object object) {

        if (object instanceof ISidedInventory) {
            return new TransactorSimple((ISidedInventory) object);
        } else if (object instanceof IInventory) {
            return new TransactorSimple(InvUtils.getInventory((IInventory) object));
        }

        return null;
    }
}
