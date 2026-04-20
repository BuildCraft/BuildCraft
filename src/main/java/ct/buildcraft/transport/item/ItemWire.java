/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.item;

import ct.buildcraft.lib.item.ItemByEnum;
import net.minecraft.world.item.DyeColor;

public class ItemWire extends ItemByEnum<DyeColor> {
	
	
    public ItemWire(Properties pro, DyeColor color) {
        super(pro, color);
    }

	@Override
	public String getDescriptionId() {
		return "item.pipewire." + type.getName();
	}
    
    
    
/*
    @Override
    public void addSubItems(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
        for (int i = 0; i < 16; i++) {
            subItems.add(new ItemStack(this, 1, i));
        }
    }
/*
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (DyeColor color : DyeColor.values()) {
            addVariant(variants, color.getId(), color.getName());
        }
    }*/
/*
    @Override
    @OnlyIn(Dist.CLIENT)
    public FontRenderer getFontRenderer(ItemStack stack) {
        return SpecialColourFontRenderer.INSTANCE;
    }
    */
}
