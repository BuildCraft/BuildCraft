/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.list;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import ct.buildcraft.api.lists.ListMatchHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public class ListMatchHandlerArmor extends ListMatchHandler {
    private static EnumSet<EquipmentSlot> getArmorTypes(ItemStack stack) {
        EnumSet<EquipmentSlot> types = EnumSet.noneOf(EquipmentSlot.class);

        for (EquipmentSlot e : EquipmentSlot.values()) {
            if (e.getType() == EquipmentSlot.Type.ARMOR) {
                if (Mob.getEquipmentSlotForItem(stack) == e) {
                    types.add(e);
                }
            }
        }

        return types;
    }

    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            EnumSet<EquipmentSlot> armorTypeIDSource = getArmorTypes(stack);
            if (armorTypeIDSource.size() > 0) {
                EnumSet<EquipmentSlot> armorTypeIDTarget = getArmorTypes(target);
                if (precise) {
                    return armorTypeIDSource.equals(armorTypeIDTarget);
                } else {
                    armorTypeIDSource.removeAll(EnumSet.complementOf(armorTypeIDTarget));
                    return armorTypeIDSource.size() > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        return getArmorTypes(stack).size() > 0;
    }
}
