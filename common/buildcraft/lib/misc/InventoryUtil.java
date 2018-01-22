/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.IInjectable;

import buildcraft.lib.inventory.ItemTransactorHelper;

public class InventoryUtil {
    // Drops

    public static void dropAll(World world, Vec3d vec, IItemHandlerModifiable handler) {
        dropAll(world, vec.xCoord, vec.yCoord, vec.zCoord, handler);
    }

    public static void dropAll(World world, BlockPos pos, IItemHandlerModifiable handler) {
        dropAll(world, pos.getX(), pos.getY(), pos.getZ(), handler);
    }

    public static void dropAll(World world, double x, double y, double z, IItemHandlerModifiable handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            drop(world, x, y, z, handler.extractItem(i, Integer.MAX_VALUE, false));
        }
    }

    public static void dropAll(World world, BlockPos pos, List<ItemStack> toDrop) {
        for (ItemStack stack : toDrop) {
            if (stack == null) {
                throw new NullPointerException("Null stack!");
            }
            drop(world, pos, stack);
        }
    }

    public static void drop(World world, BlockPos pos, @Nonnull ItemStack stack) {
        Block.spawnAsEntity(world, pos, stack);
    }

    public static void drop(World world, Vec3d vec, @Nonnull ItemStack stack) {
        drop(world, vec.xCoord, vec.yCoord, vec.zCoord, stack);
    }

    public static void drop(World world, double x, double y, double z, ItemStack stack) {
        if (stack == null) {
            return;
        }
        EntityItem entity = new EntityItem(world, x, y, z, stack);
        world.spawnEntity(entity);
    }

    // Sending items around

    /** @return The leftover stack */
    @Nullable
    public static ItemStack addToRandomInventory(World world, BlockPos pos, ItemStack stack) {
        if (stack == null) {
            return null;
        }
        List<EnumFacing> toTry = new ArrayList<>(6);
        Collections.addAll(toTry, EnumFacing.VALUES);
        Collections.shuffle(toTry);
        for (EnumFacing face : toTry) {
            TileEntity tile = world.getTileEntity(pos.offset(face));
            IItemTransactor transactor = ItemTransactorHelper.getTransactor(tile, face.getOpposite());
            stack = transactor.insert(stack, false, false);
            if (stack == null) {
                return null;
            }
        }
        return stack;
    }

    /** Look around the tile given in parameter in all 6 position, tries to add the items to a random injectable tile
     * around. Will make sure that the location from which the items are coming from (identified by the from parameter)
     * isn't used again so that entities doesn't go backwards. Returns true if successful, false otherwise. */
    @Nullable
    public static ItemStack addToRandomInjectable(World world, BlockPos pos, EnumFacing ignore,
        ItemStack stack) {
        if (stack == null) {
            return null;
        }
        List<EnumFacing> toTry = new ArrayList<>(6);
        Collections.addAll(toTry, EnumFacing.VALUES);
        Collections.shuffle(toTry);
        for (EnumFacing face : toTry) {
            if (face == ignore) continue;
            TileEntity tile = world.getTileEntity(pos.offset(face));
            IInjectable injectable = ItemTransactorHelper.getInjectable(tile, face.getOpposite());
            stack = injectable.injectItem(stack, true, face.getOpposite(), null, 0);
            if (stack == null) {
                return null;
            }
        }
        return stack;
    }

    /** Attempts to add the given stack to the best acceptor, in this order: {@link IItemHandler} instances,
     * {@link IInjectable} instances, and finally dropping it down on the ground. */
    public static void addToBestAcceptor(World world, BlockPos pos, EnumFacing ignore, @Nonnull ItemStack stack) {
        stack = addToRandomInjectable(world, pos, ignore, stack);
        stack = addToRandomInventory(world, pos, stack);
        drop(world, pos, stack);
    }

    /** Adds every stack from src to dst. Doesn't add empty stacks. */
    public static void addAll(IItemHandler src, List<ItemStack> dst) {
        for (int i = 0; i < src.getSlots(); i++) {
            ItemStack stack = src.getStackInSlot(i);
            if (stack != null) {
                dst.add(stack);
            }
        }
    }

    /** Adds the given {@link ItemStack} to the player's inventory, or drops it in front of them if their was not enough
     * room. */
    public static void addToPlayer(EntityPlayer player, ItemStack stack) {
        if (player.inventory.addItemStackToInventory(stack)) {
            player.inventoryContainer.detectAndSendChanges();
        } else {
            player.dropItem(stack, false, false);
        }
    }

    // NBT migration
}
