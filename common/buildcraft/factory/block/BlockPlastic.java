/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.block;

import buildcraft.lib.compat.MaterialBC;
import net.minecraft.block.AbstractBlock;
import net.minecraft.state.StateManager;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import java.util.List;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;

public class BlockPlastic extends BlockBCBase_Neptune {
    public BlockPlastic(String id) {
        super(MaterialBC.IRON, id);
        setDefaultState(getStateFromMeta(0));
    }

        // TODO(R.Chen): Forge createBlockState() → override appendProperties() instead.

// @Override removed (R.Chen): no longer overrides — Phase 10
    public int getMetaFromState(BlockState state) {
        DyeColor colour = state.get(BuildCraftProperties.BLOCK_COLOR);
        return colour.getId();
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().with(BuildCraftProperties.BLOCK_COLOR, DyeColor.byId(meta));
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void getSubBlocks(ItemGroup tab, DefaultedList<ItemStack> list) {
        for (DyeColor dye : DyeColor.values()) {
            list.add(new ItemStack(this, 1));
        }
    }
}
