/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.block;

import net.minecraft.block.Material;
import net.minecraft.state.StateManager;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;

public class BlockPlastic extends BlockBCBase_Neptune {
    public BlockPlastic(String id) {
        super(Material.IRON, id);
        setDefaultState(getStateFromMeta(0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BuildCraftProperties.BLOCK_COLOR);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        DyeColor colour = state.getValue(BuildCraftProperties.BLOCK_COLOR);
        return colour.getMetadata();
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BuildCraftProperties.BLOCK_COLOR, DyeColor.byMetadata(meta));
    }

    @Override
    public void getSubBlocks(ItemGroup tab, DefaultedList<ItemStack> list) {
        for (DyeColor dye : DyeColor.values()) {
            list.add(new ItemStack(this, 1, dye.getMetadata()));
        }
    }
}
