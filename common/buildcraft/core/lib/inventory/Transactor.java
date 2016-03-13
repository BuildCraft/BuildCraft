/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public abstract class Transactor implements ITransactor {
    private static final boolean DISABLE_INVENTORY_WRAPPERS = false;

    @Override
    public ItemStack add(ItemStack stack, boolean doAdd) {
        ItemStack added = stack.copy();
        added.stackSize = inject(stack, doAdd);
        return added;
    }

    public abstract int inject(ItemStack stack, boolean doAdd);

    public static ITransactor getTransactorFor(Object object, EnumFacing orientation) {
        if (object instanceof TileEntity && ((TileEntity) object).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation)) {
            return new TransactorItemHandler(((TileEntity) object).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation));
        } else if (object instanceof Entity && ((Entity) object).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation)) {
            return new TransactorItemHandler(((Entity) object).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation));
        } else if (object instanceof ItemStack && ((ItemStack) object).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation)) {
            return new TransactorItemHandler(((ItemStack) object).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation));
        } else if (!DISABLE_INVENTORY_WRAPPERS) {
            if (object instanceof ISidedInventory) {
                // TODO: Remove in 1.9
                return new TransactorItemHandler(new SidedInvWrapper((ISidedInventory) object, orientation));
            } else if (object instanceof IInventory) {
                // TODO: Remove in 1.9
                return new TransactorItemHandler(new InvWrapper((IInventory) object));
            }
        }

        return null;
    }
}
