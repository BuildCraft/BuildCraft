/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;


import buildcraft.core.block.BlockSpring;
import buildcraft.lib.item.ItemBlockBCMulti;
import net.minecraft.item.Item;

public class ItemBlockSpring extends ItemBlockBCMulti {
//    private static final String[] NAMES = {"water", "oil"};

    public ItemBlockSpring(BlockSpring block, Item.Properties properties) {
        super(block, properties);
    }

    // Calen: not still useful in 1.18.2
//    @Override
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        for(int i = 0; i < NAMES.length; i++) {
//            addVariant(variants, i, "");
//        }
//    }
}
