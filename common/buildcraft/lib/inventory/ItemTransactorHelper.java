/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.inventory.IItemTransactor.IItemInsertable;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.pipe.PipeApi;

import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.StackUtil;

public class ItemTransactorHelper {
    @Nonnull
    public static IItemTransactor getTransactor(ICapabilityProvider provider, EnumFacing face) {
        if (provider == null) {
            return NoSpaceTransactor.INSTANCE;
        }

        IItemTransactor trans = provider.getCapability(CapUtil.CAP_ITEM_TRANSACTOR, face);
        if (trans != null) {
            return trans;
        }

        IItemHandler handler = provider.getCapability(CapUtil.CAP_ITEMS, face);
        if (handler == null) {
            if (provider instanceof ISidedInventory) {
                return new SidedInventoryWrapper((ISidedInventory) provider, face);
            }
            if (provider instanceof IInventory) {
                return new InventoryWrapper((IInventory) provider);
            }
            return NoSpaceTransactor.INSTANCE;
        }
        if (handler instanceof IItemTransactor) {
            return (IItemTransactor) handler;
        }
        return new ItemHandlerWrapper(handler);
    }

    @Nonnull
    public static IItemTransactor getTransactor(InventoryPlayer inventory) {
        if (inventory == null) {
            return NoSpaceTransactor.INSTANCE;
        }
        return new InventoryWrapper(inventory);
    }

    @Nonnull
    public static IItemTransactor getTransactorForEntity(Entity entity, EnumFacing face) {
        IItemTransactor transactor = getTransactor(entity, face);
        if (transactor != NoSpaceTransactor.INSTANCE) {
            return transactor;
        } else if (entity instanceof EntityItem) {
            return new TransactorEntityItem((EntityItem) entity);
        } else if (entity instanceof EntityArrow) {
            return new TransactorEntityArrow((EntityArrow) entity);
        } else {
            return NoSpaceTransactor.INSTANCE;
        }
    }

    @Nonnull
    public static IInjectable getInjectable(ICapabilityProvider provider, EnumFacing face) {
        if (provider == null) {
            return NoSpaceInjectable.INSTANCE;
        }
        IInjectable injectable = provider.getCapability(PipeApi.CAP_INJECTABLE, face);
        if (injectable == null) {
            return NoSpaceInjectable.INSTANCE;
        }
        return injectable;
    }

    public static IItemTransactor wrapInjectable(IInjectable injectable, EnumFacing facing) {
        return new InjectableWrapper(injectable, facing);
    }

    /** Provides an implementation of {@link IItemTransactor#insert(NonNullList, boolean)} that relies on
     * {@link IItemTransactor#insert(ItemStack, boolean, boolean)}. This is the least efficient, default
     * implementation. */
    public static NonNullList<ItemStack> insertAllBypass(IItemTransactor transactor, NonNullList<ItemStack> stacks, boolean simulate) {
        NonNullList<ItemStack> leftOver = NonNullList.create();
        for (ItemStack stack : stacks) {
            ItemStack leftOverStack = transactor.insert(stack, false, simulate);
            if (!leftOverStack.isEmpty()) {
                leftOver.add(leftOverStack);
            }
        }
        return leftOver;
    }

    /** Attempts to move as many items as possible from the source {@link IItemTransactor} to the destination.
     * 
     * @return The number of items moved. */
    public static int move(IItemTransactor src, IItemTransactor dst) {
        return move(src, dst, Integer.MAX_VALUE);
    }

    /** Attempts to move up to maxItems from the source {@link IItemTransactor} to the destination.
     * 
     * @param maxItems The maximum number of items to move.
     * @return The number of items moved. */
    public static int move(IItemTransactor src, IItemTransactor dst, int maxItems) {
        return move(src, dst, null, maxItems);
    }

    /** Attempts to move up to maxItems from the source {@link IItemTransactor} to the destination.
     * 
     * @return The number of items moved. */
    public static int move(IItemTransactor src, IItemTransactor dst, IStackFilter filter) {
        return move(src, dst, filter, Integer.MAX_VALUE);
    }

    /** Attempts to move up to maxItems from the source {@link IItemTransactor} to the destination.
     * 
     * @param filter The stack filter to use - only items that match this filter will be moved.
     * @param maxItems The maximum number of items to move.
     * @return The number of items moved. */
    public static int move(IItemTransactor src, IItemTransactor dst, IStackFilter filter, int maxItems) {
        int moved = 0;
        IStackFilter rFilter = dst::canPartiallyAccept;
        if (filter != null) {
            rFilter = rFilter.and(filter);
        }
        while (true) {
            int m = moveSingle0(src, dst, rFilter, maxItems - moved, false, false);
            if (m == 0) {
                break;
            } else {
                moved += m;
            }
        }
        return moved;
    }

    public static int moveSingle(IItemTransactor src, IItemTransactor dst, IStackFilter filter, boolean simulateSrc, boolean simulateDst) {
        return moveSingle(src, dst, filter, Integer.MAX_VALUE, simulateSrc, simulateDst);
    }

    /** Similar to {@link #move(IItemTransactor, IItemTransactor, IStackFilter, int)}, but will only attempt to extract
     * and insert once, which means that you can simulate the move safely. */
    public static int moveSingle(IItemTransactor src, IItemTransactor dst, IStackFilter filter, int maxItems, boolean simulateSrc, boolean simulateDst) {
        IStackFilter rFilter = dst::canPartiallyAccept;
        if (filter != null) {
            rFilter = rFilter.and(filter);
        }
        return moveSingle0(src, dst, rFilter, maxItems, simulateSrc, simulateDst);
    }

    private static int moveSingle0(IItemTransactor src, IItemTransactor dst, IStackFilter filter, int maxItems, boolean simulateSrc, boolean simulateDst) {
        ItemStack potential = src.extract(filter, 1, maxItems, true);
        if (potential.isEmpty()) return 0;
        ItemStack leftOver = dst.insert(potential, false, simulateDst);
        int toTake = potential.getCount() - leftOver.getCount();
        IStackFilter exactFilter = (stack) -> StackUtil.canMerge(stack, potential);
        ItemStack taken = src.extract(exactFilter, toTake, toTake, simulateSrc);
        if (taken.getCount() != toTake) {
            String msg = "One of the two transactors (either src = ";
            msg += src.getClass() + " or dst = " + dst.getClass() + ")";
            msg += " didn't respect the movement flags! ( potential = " + potential;
            msg += ", leftOver = " + leftOver + ", taken = " + taken;
            msg += ", count = " + toTake + " )";
            throw new IllegalStateException(msg);
        }
        return toTake;
    }

    public static IItemInsertable createDroppingTransactor(World world, Vec3d vec) {
        return (stack, allorNone, simulate) -> {
            if (!simulate) {
                InventoryUtil.drop(world, vec, stack);
            }
            return StackUtil.EMPTY;
        };
    }
}
