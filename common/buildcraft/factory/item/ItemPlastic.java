/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.item;

import buildcraft.factory.block.BlockPlastic;
import buildcraft.lib.item.ItemBlockBCMulti;
import buildcraft.lib.misc.ColourUtil;
import net.minecraft.world.item.Item;

import java.util.Locale;

public class ItemPlastic extends ItemBlockBCMulti {
    public ItemPlastic(BlockPlastic block, Item.Properties properties) {
//        super(block, createNameArray());
        super(block, properties);
//        this.setMaxDamage(0);
//        this.setHasSubtypes(true);
    }

    private static String[] createNameArray() {
        String[] arr = ColourUtil.getNameArray();
        String[] switched = new String[16];
        for (int i = 0; i < arr.length; i++) {
            switched[15 - i] = arr[i].toLowerCase(Locale.ROOT);
        }
        return switched;
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        for (EnumDyeColor colour : EnumDyeColor.values()) {
//            addVariant(variants, colour.getMetadata(), colour.getName());
//        }
//    }
}
