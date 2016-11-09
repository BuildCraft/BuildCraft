/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.bpt.player;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.bpt.IMaterialProvider;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;

import buildcraft.lib.bpt.builder.RequestedFree.FreeFluid;
import buildcraft.lib.bpt.builder.RequestedFree.FreeItem;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.StackUtil;

public class BuilderPlayer implements IMaterialProvider {
    public final EntityPlayer player;
    public final boolean isStuffFree;
    private final List<IRequested> allRequested = new ArrayList<>();

    public BuilderPlayer(EntityPlayer player) {
        this.player = player;
        isStuffFree = player.capabilities.isCreativeMode;
    }

    @Override
    public IRequestedItem requestStack(ItemStack stack) {
        if (isStuffFree) {
            return new FreeItem(stack);
        } else {
            IRequestedItem req = new RequestedItemStack(stack);
            allRequested.add(req);
            return req;
        }
    }

    @Override
    public IRequestedItem requestStackForBlock(IBlockState state) {
        ItemStack wanted = StackUtil.getItemStackForState(state);
        if (wanted == null) {
            throw new IllegalStateException("Unknown item block " + state);
        }
        return requestStack(wanted);
    }

    @Override
    public IRequestedFluid requestFluid(FluidStack fluid) {
        return new FreeFluid(fluid);
    }

    public void releaseAll() {
        for (IRequested req : allRequested) {
            req.release();
        }
        allRequested.clear();
    }

    private class RequestedItemStack implements IRequestedItem {
        private final ItemStack requested, copy;
        private boolean locked = false, used = false;
        private ItemStack held;

        public RequestedItemStack(ItemStack requested) {
            this.requested = requested;
            this.copy = requested.copy();
        }

        @Override
        public boolean lock() throws IllegalStateException {
            if (used) {
                throw new IllegalStateException("Already used!");
            } else {
                IItemTransactor trans = ItemTransactorHelper.getTransactor(player.inventory);
                IStackFilter filter = (stack) -> {
                    copy.stackSize = stack.stackSize;
                    return ItemStack.areItemsEqual(stack, copy);
                };
                held = trans.extract(filter, requested.stackSize, requested.stackSize, false);
                if (held != null) {
                    locked = true;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isLocked() {
            return locked || used;
        }

        @Override
        public void use() throws IllegalStateException {
            if (used) {
                throw new IllegalStateException("Already used!");
            }
            if (!locked) {
                throw new IllegalStateException("Not locked!");
            }
            locked = false;
            used = true;
        }

        @Override
        public void release() {
            if (locked) {
                IItemTransactor trans = ItemTransactorHelper.getTransactor(player.inventory);
                ItemStack leftOver = trans.insert(held, false, false);
                if (leftOver != null) {
                    InventoryUtil.drop(player.worldObj, player.posX, player.posY, player.posZ, leftOver);
                }
                held = null;
                locked = false;
            }
            used = true;
        }

        @Override
        public ItemStack getRequested() {
            return requested;
        }
    }
}
