/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.item;

import ct.buildcraft.api.enums.EnumRedstoneChipset;
import ct.buildcraft.lib.item.ItemByEnum;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemRedstoneChipset extends ItemByEnum<EnumRedstoneChipset> {
	
    public ItemRedstoneChipset(Properties prop, EnumRedstoneChipset type) {
        super(prop, type);
        //setHasSubtypes(true);
    }

/*    @Override
    @OnlyI(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (EnumRedstoneChipset type : EnumRedstoneChipset.values()) {
            addVariant(variants, type.ordinal(), type.getName());
        }
    }*/
    
	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
	      if (this.allowedIn(tab)) {
	          list.add(new ItemStack(this));
	       }
	}

	@Override
	public String getDescriptionId(ItemStack p_41455_) {
		return "item.buildcraftsilicon.redstone_" + type.getSerializedName() + "_chipset";
	}
}
