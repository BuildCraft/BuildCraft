/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.font.SpecialColourFontRenderer;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.ColourUtil;

public class ItemWire extends ItemBC_Neptune {
    public ItemWire(String id) {
        super(id);
        setHasSubtypes(true);
    }

    @Override
    public void addSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        for (int i = 0; i < 16; i++) {
            subItems.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (EnumDyeColor color : EnumDyeColor.values()) {
            addVariant(variants, color.getMetadata(), color.getName());
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return ColourUtil.getTextFullTooltipSpecial(EnumDyeColor.byMetadata(stack.getMetadata())) + " " + super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public FontRenderer getFontRenderer(ItemStack stack) {
        return SpecialColourFontRenderer.INSTANCE;
    }
}
