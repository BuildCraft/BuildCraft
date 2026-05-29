/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import javax.annotation.Nonnull;

import net.minecraft.util.collection.DefaultedList;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// STUB(R.Chen): the Forge IItemHandler-based dropAll overloads and the inventory-routing helpers
// (addToRandomInventory / IItemTransactor / ItemTransactorHelper) are dropped until the Transfer-API
// item layer is migrated. Only the BlockPos + DefaultedList drop path used by TileBC_Neptune is kept.
public class InventoryUtil {

    public static void dropAll(World world, BlockPos pos, DefaultedList<ItemStack> toDrop) {
        for (ItemStack stack : toDrop) {
            if (stack == null) {
                throw new NullPointerException("Null stack!");
            }
            drop(world, pos, stack);
        }
    }

    public static void drop(World world, BlockPos pos, @Nonnull ItemStack stack) {
        drop(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
    }

    public static void drop(World world, double x, double y, double z, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        world.spawnEntity(new ItemEntity(world, x, y, z, stack));
    }
}
