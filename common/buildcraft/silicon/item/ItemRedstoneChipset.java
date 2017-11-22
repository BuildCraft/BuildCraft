/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.item;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.enums.EnumRedstoneChipset;

import buildcraft.lib.item.ItemBC_Neptune;

public class ItemRedstoneChipset extends ItemBC_Neptune {
    public ItemRedstoneChipset(String id) {
        super(id);
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (EnumRedstoneChipset type : EnumRedstoneChipset.values()) {
            addVariant(variants, type.ordinal(), type.getName());
        }
    }

    @Override
    public void addSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        for (EnumRedstoneChipset type : EnumRedstoneChipset.values()) {
            subItems.add(new ItemStack(this, 1, type.ordinal()));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item.redstone_" + EnumRedstoneChipset.values()[stack.getMetadata()].getName() + "_chipset";
    }
}
