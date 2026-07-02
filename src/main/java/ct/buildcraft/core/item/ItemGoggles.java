/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class ItemGoggles extends ArmorItem {
  //  private static final ArmorProperties HELMET_PROPERTIES = new ArmorProperties(0, 0, 0);
//    private final String id;

    public ItemGoggles(String id) {
        super(ArmorMaterials.CHAIN, EquipmentSlot.HEAD, new Item.Properties().defaultDurability(-1).tab(CreativeModeTab.TAB_COMBAT));
//        this.id = id;
//        init();
    }

/*    @Override
    public String id() {
        return id;
    }*/

/*    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
        return HELMET_PROPERTIES;
    }*/

    @Override
    public float getToughness() {
        return 0;
     }
}
