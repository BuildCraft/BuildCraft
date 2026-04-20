/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import ct.buildcraft.api.inventory.IItemTransactor;
import ct.buildcraft.api.transport.IInjectable;
import ct.buildcraft.lib.inventory.ItemTransactorHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class InventoryUtil {
    // Drops

    public static void dropAll(Level world, Vec3 vec, IItemHandlerModifiable handler) {
        dropAll(world, vec.x, vec.y, vec.z, handler);
    }

    public static void dropAll(Level world, BlockPos pos, IItemHandlerModifiable handler) {
        dropAll(world, pos.getX(), pos.getY(), pos.getZ(), handler);
    }

    public static void dropAll(Level world, double x, double y, double z, IItemHandlerModifiable handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
        	Containers.dropItemStack(world, x, y, z, handler.extractItem(i, Integer.MAX_VALUE, false));
        }
    }

    public static void dropAll(Level world, BlockPos pos, NonNullList<ItemStack> toDrop) {
        for (ItemStack stack : toDrop) {
            if (stack == null) {
                throw new NullPointerException("Null stack!");
            }
            drop(world, pos, stack);
        }
    }

    public static void drop(Level world, BlockPos pos, @Nonnull ItemStack stack) {
        Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    public static void drop(Level world, Vec3 vec, @Nonnull ItemStack stack) {
    	Containers.dropItemStack(world, vec.x, vec.y, vec.z, stack);
    }


    // Sending items around

    /** @return The leftover stack */
    @Nonnull
    public static ItemStack addToRandomInventory(Level world, BlockPos pos, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return StackUtil.EMPTY;
        }
        List<Direction> toTry = new ArrayList<>(6);
        Collections.addAll(toTry, Direction.values());
        Collections.shuffle(toTry);
        for (Direction face : toTry) {
            BlockEntity tile = world.getBlockEntity(pos.offset(face.getNormal()));
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
    public static ItemStack addToRandomInjectable(Level world, BlockPos pos, Direction ignore,
        @Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return StackUtil.EMPTY;
        }
        List<Direction> toTry = new ArrayList<>(6);
        Collections.addAll(toTry, Direction.values());
        Collections.shuffle(toTry);
        for (Direction face : toTry) {
            if (face == ignore) continue;
            BlockEntity tile = world.getBlockEntity(pos.offset(face.getNormal()));
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
    public static void addToBestAcceptor(Level world, BlockPos pos, Direction ignore, @Nonnull ItemStack stack) {
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
    public static void addToPlayer(Player player, ItemStack stack) {
        if (player.getInventory().add(stack)) {
            player.containerMenu.broadcastChanges();;
        } else {
            player.drop(stack, false, false);
        }
    }

    // NBT migration
}
