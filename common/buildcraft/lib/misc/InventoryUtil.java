/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.IInjectable;
import buildcraft.lib.inventory.InventoryIterator;
import buildcraft.lib.inventory.ItemTransactorHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventoryUtil {
    // Drops

    public static void dropAll(World world, Vector3d vec, IItemHandlerModifiable handler) {
        dropAll(world, vec.x, vec.y, vec.z, handler);
    }

    public static void dropAll(World world, BlockPos pos, IItemHandlerModifiable handler) {
        dropAll(world, pos.getX(), pos.getY(), pos.getZ(), handler);
    }

    public static void dropAll(World world, double x, double y, double z, IItemHandlerModifiable handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            drop(world, x, y, z, handler.extractItem(i, Integer.MAX_VALUE, false));
        }
    }

    public static void dropAll(World world, BlockPos pos, NonNullList<ItemStack> toDrop) {
        for (ItemStack stack : toDrop) {
            if (stack == null) {
                throw new NullPointerException("Null stack!");
            }
            drop(world, pos, stack);
        }
    }

    public static void drop(World world, BlockPos pos, @Nonnull ItemStack stack) {
        world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack));
    }

    public static void drop(World world, Vector3d vec, @Nonnull ItemStack stack) {
        drop(world, vec.x, vec.y, vec.z, stack);
    }

    public static void drop(World world, double x, double y, double z, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemEntity entity = new ItemEntity(world, x, y, z, stack);
        world.addFreshEntity(entity);
    }

    // Sending items around

    /** @return The leftover stack */
    @Nonnull
    public static ItemStack addToRandomInventory(World world, BlockPos pos, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return StackUtil.EMPTY;
        }
        List<Direction> toTry = new ArrayList<>(6);
        Collections.addAll(toTry, Direction.values());
        Collections.shuffle(toTry);
        for (Direction face : toTry) {
            TileEntity tile = world.getBlockEntity(pos.relative(face));
            IItemTransactor transactor = ItemTransactorHelper.getTransactor(tile, face.getOpposite());
            stack = transactor.insert(stack, false, false);
            if (stack.isEmpty()) {
                return StackUtil.EMPTY;
            }
        }
        return stack;
    }

    /** Look around the tile given in parameter in all 6 position, tries to add the items to a random injectable tile
     * around. Will make sure that the location from which the items are coming from (identified by the from parameter)
     * isn't used again so that entities doesn't go backwards. Returns true if successful, false otherwise. */
    @Nonnull
    public static ItemStack addToRandomInjectable(World world, BlockPos pos, Direction ignore, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return StackUtil.EMPTY;
        }
        List<Direction> toTry = new ArrayList<>(6);
        Collections.addAll(toTry, Direction.values());
        Collections.shuffle(toTry);
        for (Direction face : toTry) {
            if (face == ignore) continue;
            TileEntity tile = world.getBlockEntity(pos.relative(face));
            IInjectable injectable = ItemTransactorHelper.getInjectable(tile, face.getOpposite());
            stack = injectable.injectItem(stack, true, face.getOpposite(), null, 0);
            if (stack.isEmpty()) {
                return StackUtil.EMPTY;
            }
        }
        return stack;
    }

    /** Attempts to add the given stack to the best acceptor, in this order: {@link IItemHandler} instances,
     * {@link IInjectable} instances, and finally dropping it down on the ground. */
    public static void addToBestAcceptor(World world, BlockPos pos, Direction ignore, @Nonnull ItemStack stack) {
        stack = addToRandomInjectable(world, pos, ignore, stack);
        stack = addToRandomInventory(world, pos, stack);
        drop(world, pos, stack);
    }

    /** Adds every stack from src to dst. Doesn't add empty stacks. */
    public static void addAll(IItemHandler src, NonNullList<ItemStack> dst) {
        for (int i = 0; i < src.getSlots(); i++) {
            ItemStack stack = src.getStackInSlot(i);
            if (!stack.isEmpty()) {
                dst.add(stack);
            }
        }
    }

    /** Adds the given {@link ItemStack} to the player's inventory, or drops it in front of them if their was not enough
     * room. */
    public static void addToPlayer(PlayerEntity player, ItemStack stack) {
//        if (player.inventory.addItemStackToInventory(stack))
        if (player.inventory.add(stack)) {
//            player.inventoryContainer.detectAndSendChanges();
            player.containerMenu.broadcastChanges();
        } else {
            player.drop(stack, false, false);
        }
    }

    // NBT migration

    // Calen: from 1.8.9

    public static IInvSlot getItem(IItemHandler inv, IStackFilter filter) {
        for (IInvSlot s : InventoryIterator.getIterable(inv)) {
            // if (s.getStackInSlot() != null && filter.matches(s.getStackInSlot()))
            if (!s.getStackInSlot().isEmpty() && filter.matches(s.getStackInSlot())) {
                return s;
            }
        }

        return null;
    }

    /** Ensures that the given inventory is the full inventory, i.e. takes double chests into account.
     *
     * @param inv
     * @return Modified inventory if double chest, unmodified otherwise. */
    public static IInventory getInventory(IInventory inv) {
        if (inv instanceof ChestTileEntity) {
            ChestTileEntity adjacent = BlockUtil.getOtherDoubleChest((ChestTileEntity) inv);
            if (adjacent != null) {
                // return new InventoryLargeChest("", (TileEntityChest) inv, adjacent);
                return new DoubleSidedInventory(inv, adjacent);
            }
            return inv;
        }
        return inv;
    }

    public static ItemStack insert(IItemHandler itemHandler, ItemStack stack, boolean simulate) {
        ItemStack overflow = stack.copy();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            overflow = itemHandler.insertItem(i, overflow, simulate);
            if (overflow.isEmpty()) {
                break;
            }
        }
        return overflow;
    }
}
