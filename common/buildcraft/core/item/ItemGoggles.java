/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// TODO(R.Chen): ISpecialArmor removed in 1.20.1 — needs Fabric armor attribute override equivalent
package buildcraft.core.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;

import buildcraft.lib.item.IItemBuildCraft;

public class ItemGoggles extends ArmorItem implements IItemBuildCraft {
    private final String id;

    public ItemGoggles(String id) {
        super(ArmorMaterials.CHAIN, ArmorItem.Type.HELMET, new Item.Settings());
        this.id = id;
        init();
    }

    @Override
    public String id() {
        return id;
    }
}
