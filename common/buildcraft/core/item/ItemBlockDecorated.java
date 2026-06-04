/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.util.ModelIdentifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.enums.EnumDecoratedBlock;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.item.ItemBlockBCMulti;

public class ItemBlockDecorated extends ItemBlockBCMulti {

    public ItemBlockDecorated(BlockBCBase_Neptune block) {
        super(block, createNameArray());
    }

    private static String[] createNameArray() {
        String[] names = new String[EnumDecoratedBlock.VALUES.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = EnumDecoratedBlock.VALUES[i].getName();
        }
        return names;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    @Environment(EnvType.CLIENT)
    public void addModelVariants(HashMap<Integer, ModelIdentifier> variants) {
        for (EnumDecoratedBlock type : EnumDecoratedBlock.VALUES) {
            addVariant(variants, type.ordinal(), type.getName());
        }
    }
}
