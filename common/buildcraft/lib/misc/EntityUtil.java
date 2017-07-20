/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.tools.IToolWrench;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntityUtil {
    public static NonNullList<ItemStack> collectItems(World world, BlockPos around, double radius) {
        return collectItems(world, new Vec3d(around).addVector(0.5, 0.5, 0.5), radius);
    }

    public static NonNullList<ItemStack> collectItems(World world, Vec3d around, double radius) {
        NonNullList<ItemStack> stacks = NonNullList.create();

        AxisAlignedBB aabb = BoundingBoxUtil.makeAround(around, radius);
        for (EntityItem ent : world.getEntitiesWithinAABB(EntityItem.class, aabb)) {
            if (!ent.isDead) {
                ent.isDead = true;
                stacks.add(ent.getEntityItem());
            }
        }
        return stacks;
    }

    public static Vec3d getVec(Entity entity) {
        return new Vec3d(entity.posX, entity.posY, entity.posZ);
    }

    public static EnumHand getWrenchHand(EntityLivingBase entity) {
        ItemStack stack = entity.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return EnumHand.MAIN_HAND;
        }
        stack = entity.getHeldItemOffhand();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return EnumHand.OFF_HAND;
        }
        return null;
    }

    public static void activateWrench(EntityPlayer player) {
        ItemStack stack = player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, EnumHand.MAIN_HAND, stack, null);
            return;
        }
        stack = player.getHeldItemOffhand();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, EnumHand.OFF_HAND, stack, null);
        }
    }

    @Nonnull
    public static ItemStack getArrowStack(EntityArrow arrow) {
        // FIXME: Replace this with an invocation of arrow.getArrowStack
        // (but its protected so we can't)
        if (arrow instanceof EntitySpectralArrow) {
            return new ItemStack(Items.SPECTRAL_ARROW);
        }
        return new ItemStack(Items.ARROW);
    }
}
