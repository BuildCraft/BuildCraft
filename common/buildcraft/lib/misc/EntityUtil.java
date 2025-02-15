/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.tools.IToolWrench;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class EntityUtil {
    public static NonNullList<ItemStack> collectItems(Level world, BlockPos around, double radius) {
        return collectItems(world, new Vec3(around.getX(), around.getY(), around.getZ()).add(0.5, 0.5, 0.5), radius);
    }

    public static NonNullList<ItemStack> collectItems(Level world, Vec3 around, double radius) {
        NonNullList<ItemStack> stacks = NonNullList.create();

        AABB aabb = BoundingBoxUtil.makeAround(around, radius);
        for (ItemEntity ent : world.getEntitiesOfClass(ItemEntity.class, aabb)) {
            if (ent.isAlive()) {
                ent.kill();
                stacks.add(ent.getItem());
            }
        }
        return stacks;
    }

    public static Vec3 getVec(Entity entity) {
        return new Vec3(entity.getX(), entity.getY(), entity.getZ());
    }

    public static void setVec(Entity entity, Vec3 vec) {
        entity.setPos(vec.x, vec.y, vec.z);
    }

    public static InteractionHand getWrenchHand(LivingEntity entity) {
        ItemStack stack = entity.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return InteractionHand.MAIN_HAND;
        }
        stack = entity.getOffhandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    public static void activateWrench(Player player, HitResult trace) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, InteractionHand.MAIN_HAND, stack, trace);
            return;
        }
        stack = player.getOffhandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, InteractionHand.OFF_HAND, stack, trace);
        }
    }

    @Nonnull
//    public static ItemStack getArrowStack(EntityArrow arrow)
    public static ItemStack getArrowStack(AbstractArrow arrow) {
        // FIXME: Replace this with an invocation of arrow.getArrowStack
        // (but its protected so we can't)
//        if (arrow instanceof EntitySpectralArrow)
        if (arrow instanceof SpectralArrow) {
            return new ItemStack(Items.SPECTRAL_ARROW);
        }
        return new ItemStack(Items.ARROW);
    }

    // Calen 1.20.1
    public static EntityType<?> getItemFromRegistryName(String name) {
        return getItemFromRegistryName(new ResourceLocation(name));
    }

    public static EntityType<?> getItemFromRegistryName(ResourceLocation name) {
        return ForgeRegistries.ENTITY_TYPES.getValue(name);
    }

    public static ResourceLocation getRegistryName(EntityType<?> type) {
        return type.builtInRegistryHolder().key().location();
    }
}
