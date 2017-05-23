/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import buildcraft.core.block.BlockSpring;
import buildcraft.lib.item.ItemBlockBCMulti;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

public class ItemBlockSpring extends ItemBlockBCMulti {
    private static final String[] NAMES = { "water", "oil" };

    public ItemBlockSpring(BlockSpring block) {
        super(block, NAMES);
    }

    @Override
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for(int i = 0; i < NAMES.length; i++) {
            addVariant(variants, i, "");
        }
    }
}
