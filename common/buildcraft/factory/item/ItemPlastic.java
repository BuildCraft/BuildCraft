/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.item;

import java.util.Locale;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.DyeColor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.item.ItemBlockBCMulti;
import buildcraft.lib.misc.ColourUtil;

import buildcraft.factory.block.BlockPlastic;

public class ItemPlastic extends ItemBlockBCMulti {
    public ItemPlastic(BlockPlastic block) {
        super(block, createNameArray());
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    private static String[] createNameArray() {
        String[] arr = ColourUtil.getNameArray();
        String[] switched = new String[16];
        for (int i = 0; i < arr.length; i++) {
            switched[15 - i] = arr[i].toLowerCase(Locale.ROOT);
        }
        return switched;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelIdentifier> variants) {
        for (DyeColor colour : DyeColor.values()) {
            addVariant(variants, colour.getMetadata(), colour.getName());
        }
    }
}
