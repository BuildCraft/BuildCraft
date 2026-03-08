/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.ColourUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public class BlockPlastic extends BlockBCBase_Neptune {
    // TODO Calen: BlockPlastic not reg in 1.12.2
    public BlockPlastic(String idBC, Properties props) {
        super(idBC, props);
//        setDefaultState(getStateFromMeta(0));
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(BuildCraftProperties.BLOCK_COLOR, DyeColor.byId(0))
        );
    }

    @Override
//    protected BlockStateContainer createBlockState()
    protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BuildCraftProperties.BLOCK_COLOR);
    }

//    @Override
//    public int getMetaFromState(IBlockState state) {
//        EnumDyeColor colour = state.getValue(BuildCraftProperties.BLOCK_COLOR);
//        return colour.getMetadata();
//    }

//    @Override
//    public IBlockState getStateFromMeta(int meta) {
//        return getDefaultState().withProperty(BuildCraftProperties.BLOCK_COLOR, EnumDyeColor.byMetadata(meta));
//    }

    @Override
//    public void getSubBlocks(ItemGroup tab, NonNullList<ItemStack> list)
    public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> list) {
        for (DyeColor dye : DyeColor.values()) {
//            list.add(new ItemStack(this, 1, dye.getMetadata()));
            ItemStack stack = new ItemStack(this, 1);
            ColourUtil.addColourTagToStack(stack, dye);
            list.add(stack);
        }
    }
}
