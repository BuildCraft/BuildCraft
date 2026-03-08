/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.ColourUtil;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ItemWire extends ItemBC_Neptune {
    public ItemWire(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        setHasSubtypes(true);
    }

    @Override
    public void addSubItems(ItemGroup tab, NonNullList<ItemStack> subItems) {
        for (int i = 0; i < 16; i++) {
            // Calen: meta -> tag
            ItemStack stack = new ItemStack(this, 1);
            subItems.add(ColourUtil.addColourTagToStack(stack, i));
        }
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        for (EnumDyeColor color : EnumDyeColor.values()) {
//            addVariant(variants, color.getMetadata(), color.getName());
//        }
//    }

    @Override
//    public String getItemStackDisplayName(ItemStack stack)
    public ITextComponent getName(ItemStack stack) {
//        return ColourUtil.getTextFullTooltipSpecial(EnumDyeColor.byMetadata(stack.getMetadata())) + " " + super.getItemStackDisplayName(stack);

        DyeColor colour = ColourUtil.getStackColourFromTag(stack);
        String prefix = colour == null ? "" : (ColourUtil.getTextFullTooltipSpecial(ColourUtil.getStackColourFromTag(stack)) + " ");
        return new StringTextComponent(prefix).append(super.getName(stack));
    }

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public FontRenderer getFontRenderer(ItemStack stack) {
//        return SpecialColourFontRenderer.INSTANCE;
//    }
}
