/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// TODO(R.Chen): blocked by lib.item.ItemBlockBCMulti (not yet migrated to Fabric 1.20.1)
package buildcraft.core.item;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.util.ModelIdentifier;

import buildcraft.lib.item.ItemBlockBCMulti;

import buildcraft.core.block.BlockSpring;

public class ItemBlockSpring extends ItemBlockBCMulti {
    private static final String[] NAMES = { "water", "oil" };

    public ItemBlockSpring(BlockSpring block) {
        super(block, NAMES);
    }

    @Override
    public void addModelVariants(TIntObjectHashMap<ModelIdentifier> variants) {
        for(int i = 0; i < NAMES.length; i++) {
            addVariant(variants, i, "");
        }
    }
}
