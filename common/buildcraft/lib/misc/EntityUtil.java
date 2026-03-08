/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.tools.IToolWrench;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntityUtil {
    public static NonNullList<ItemStack> collectItems(World world, BlockPos around, double radius) {
        return collectItems(world, new Vector3d(around.getX(), around.getY(), around.getZ()).add(0.5, 0.5, 0.5), radius);
    }

    public static NonNullList<ItemStack> collectItems(World world, Vector3d around, double radius) {
        NonNullList<ItemStack> stacks = NonNullList.create();

        AxisAlignedBB aabb = BoundingBoxUtil.makeAround(around, radius);
        for (ItemEntity ent : world.getEntitiesOfClass(ItemEntity.class, aabb)) {
            if (ent.isAlive()) {
                ent.kill();
                stacks.add(ent.getItem());
            }
        }
        return stacks;
    }

    public static Vector3d getVec(Entity entity) {
        return new Vector3d(entity.getX(), entity.getY(), entity.getZ());
    }

    public static void setVec(Entity entity, Vector3d vec) {
        entity.setPos(vec.x, vec.y, vec.z);
    }

    public static Hand getWrenchHand(LivingEntity entity) {
        ItemStack stack = entity.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return Hand.MAIN_HAND;
        }
        stack = entity.getOffhandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    public static void activateWrench(PlayerEntity player, RayTraceResult trace) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, Hand.MAIN_HAND, stack, trace);
            return;
        }
        stack = player.getOffhandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, Hand.OFF_HAND, stack, trace);
        }
    }

    @Nonnull
//    public static ItemStack getArrowStack(EntityArrow arrow)
    public static ItemStack getArrowStack(AbstractArrowEntity arrow) {
        // FIXME: Replace this with an invocation of arrow.getArrowStack
        // (but its protected so we can't)
//        if (arrow instanceof EntitySpectralArrow)
        if (arrow instanceof SpectralArrowEntity) {
            return new ItemStack(Items.SPECTRAL_ARROW);
        }
        return new ItemStack(Items.ARROW);
    }
}
