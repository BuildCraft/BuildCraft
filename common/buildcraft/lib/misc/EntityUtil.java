/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.tools.IToolWrench;

public class EntityUtil {
    public static DefaultedList<ItemStack> collectItems(World world, BlockPos around, double radius) {
        return collectItems(world, new Vec3d(around.getX(), around.getY(), around.getZ()).add(0.5, 0.5, 0.5), radius);
    }

    public static DefaultedList<ItemStack> collectItems(World world, Vec3d around, double radius) {
        DefaultedList<ItemStack> stacks = DefaultedList.of();

        Box aabb = BoundingBoxUtil.makeAround(around, radius);
        for (ItemEntity ent : world.getEntitiesWithinAABB(ItemEntity.class, aabb)) {
            if (!ent.isRemoved()) {
                ent.remove(net.minecraft.entity.Entity.RemovalReason.DISCARDED);
                stacks.add(ent.getItem());
            }
        }
        return stacks;
    }

    public static Vec3d getVec(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
    }

    public static void setVec(Entity entity, Vec3d vec) {
        entity.setPosition(vec.x, vec.y, vec.z);
    }

    public static Hand getWrenchHand(LivingEntity entity) {
        ItemStack stack = entity.getMainHandStack();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return Hand.MAIN_HAND;
        }
        stack = entity.getOffHandStack();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    public static void activateWrench(PlayerEntity player, HitResult trace) {
        ItemStack stack = player.getMainHandStack();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, Hand.MAIN_HAND, stack, trace);
            return;
        }
        stack = player.getOffHandStack();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, Hand.OFF_HAND, stack, trace);
        }
    }

    @Nonnull
    public static ItemStack getArrowStack(EntityArrow arrow) {
        // FIXME: Replace this with an invocation of arrow.getArrowStack
        // (but its protected so we can't)
        if (arrow instanceof SpectralArrowEntity) {
            return new ItemStack(Items.SPECTRAL_ARROW);
        }
        return new ItemStack(Items.ARROW);
    }
}
