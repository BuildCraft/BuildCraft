/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import buildcraft.api.core.IStackFilter;

public class TransactorItemHandler extends Transactor {

    protected IItemHandler handler;

    public TransactorItemHandler(IItemHandler handler) {
        this.handler = handler;
    }

    @Override
    public int inject(ItemStack stack, boolean doAdd) {
        List<Integer> filledSlots = new ArrayList<>(handler.getSlots());
        List<Integer> emptySlots = new ArrayList<>(handler.getSlots());
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack simulated = handler.insertItem(i, stack, true);
            if (simulated == null || simulated.stackSize < stack.stackSize) {
                if (handler.getStackInSlot(i) == null) {
                    emptySlots.add(i);
                } else {
                    filledSlots.add(i);
                }
            }
        }

        int injected = 0;
        injected = tryPut(stack, filledSlots, injected, doAdd);
        injected = tryPut(stack, emptySlots, injected, doAdd);
        return injected;
    }

    private int tryPut(ItemStack stack, List<Integer> slots, int injected, boolean doAdd) {
        int realInjected = injected;
        ItemStack toInsert = null;

        if (realInjected >= stack.stackSize) {
            return realInjected;
        }

        for (int i : slots) {
            if (toInsert == null) {
                toInsert = stack.copy();
                toInsert.stackSize = stack.stackSize - realInjected;
            }
            int oldInjected = realInjected;
            ItemStack insertedStack = handler.insertItem(i, toInsert, !doAdd);
            if (insertedStack == null) {
                realInjected += stack.stackSize;
            } else {
                realInjected += stack.stackSize - insertedStack.stackSize;
            }
            if (realInjected >= stack.stackSize) {
                return realInjected;
            } else if (oldInjected != realInjected) {
                toInsert = null;
            }
        }

        return realInjected;
    }

    @Override
    public ItemStack remove(IStackFilter filter, boolean doRemove) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack != null && filter.matches(stack)) {
                return handler.extractItem(i, 1, !doRemove);
            }
        }
        return null;
    }
}
