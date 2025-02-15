/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import buildcraft.lib.item.IItemBuildCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

//public class ItemGoggles extends ArmorItem implements IItemBuildCraft, ISpecialArmor
public class ItemGoggles extends ArmorItem implements IItemBuildCraft {
    //    private static final ArmorProperties HELMET_PROPERTIES = new ArmorProperties(0, 0, 0);
//    private static final Item.Properties HELMET_PROPERTIES = new Item.Properties().;
    private final String idBC;
//    private final String nameSpace;
//    private final ResourceLocation id;

    public ItemGoggles(String idBC, Item.Properties properties) {
        super(ArmorMaterials.CHAIN, Type.HELMET, properties);
//        super(ArmorMaterials.CHAIN, 0, EquipmentSlot.HEAD);
        this.idBC = idBC;
        init();
    }

    @Override
    public String getIdBC() {
        return idBC;
    }

    // Calen
    private String unlocalizedName;

    @Override
    public void setUnlocalizedName(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.unlocalizedName;
    }

//    @Override
//    public ArmorProperties getProperties(LivingEntity player, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
//        return HELMET_PROPERTIES;
//    }

    @Override
//    public int getArmorDisplay(Player player, @Nonnull ItemStack armor, int slot)
    public int getDamage(ItemStack stack) {
        return 0;
    }

    @Override
//    public void damageArmor(LivingEntity entity, @Nonnull ItemStack stack, DamageSource source, int damage, int slot)
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        // Invulnerable goggles
        return 0;
    }

    // Calen 1.20.1
    public ResourceLocation getRegistryName() {
        return ForgeRegistries.ITEMS.getKey(this);
    }
}
