/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import buildcraft.api.lists.ListMatchHandler;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class ListMatchHandlerArmor extends ListMatchHandler {
    private static EnumSet<EquipmentSlotType> getArmorTypes(ItemStack stack) {
//        EntityPlayer player = BCLibProxy.getProxy().getClientPlayer();
//        if (player == null) {
//            player = BuildCraftAPI.fakePlayerProvider.getBuildCraftPlayer(DimensionManager.getWorld(0));
//        }
        EnumSet<EquipmentSlotType> types = EnumSet.noneOf(EquipmentSlotType.class);

        for (EquipmentSlotType e : EquipmentSlotType.values()) {
            if (e.getType() == EquipmentSlotType.Group.ARMOR) {
                // Calen: IForgeItem#canEquip
//                if (stack.getItem().canEquip(stack, e, player))
                if (MobEntity.getEquipmentSlotForItem(stack) == e) {
                    types.add(e);
                }
            }
        }

        return types;
    }

    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            EnumSet<EquipmentSlotType> armorTypeIDSource = getArmorTypes(stack);
            if (armorTypeIDSource.size() > 0) {
                EnumSet<EquipmentSlotType> armorTypeIDTarget = getArmorTypes(target);
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
