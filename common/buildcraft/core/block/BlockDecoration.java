/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;

import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.lib.block.BlockBCBase_Neptune;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class BlockDecoration extends BlockBCBase_Neptune {
    // Calen: create different blocks instead of meta/blockState
//    public static final Property<EnumDecoratedBlock> DECORATED_TYPE = BuildCraftProperties.DECORATED_BLOCK;
    public final EnumDecoratedBlock DECORATED_TYPE;

    public BlockDecoration(String idBC, AbstractBlock.Properties properties, EnumDecoratedBlock decoratedType) {
//        super(Material.IRON, id);
        super(idBC, properties);
//        setDefaultState(getDefaultState().withProperty(DECORATED_TYPE, EnumDecoratedBlock.DESTROY));
        this.DECORATED_TYPE = decoratedType;
    }

    // IBlockState

//    @Override
////    protected BlockStateContainer createBlockState()
//    protected void createBlockStateDefinition(@Nonnull StateContainer.IBuilder<Block, BlockState> builder) {
////        return new BlockStateContainer(this, DECORATED_TYPE);
////        builder.add(DECORATED_TYPE);
//    }

//    @Override
//    public IBlockState getStateFromMeta(int meta) {
//        IBlockState state = getDefaultState();
//        return state.withProperty(DECORATED_TYPE, EnumDecoratedBlock.fromMeta(meta));
//    }

//    @Override
//    public int getMetaFromState(IBlockState state) {
//        return state.getValue(DECORATED_TYPE).ordinal();
//    }

    // Other

//    @Override
//    public void getSubBlocks(ItemGroup tab, NonNullList<ItemStack> list) {
//        for (EnumDecoratedBlock type : EnumDecoratedBlock.values()) {
//            list.add(new ItemStack(this, 1, type.ordinal()));
//        }
//    }

    // Calen: use datagen LootTable
//    @Override
//    public int damageDropped(IBlockState state) {
//        return state.getValue(DECORATED_TYPE).ordinal();
//    }

    @Override
    public int getLightValue(BlockState state, IBlockReader level, BlockPos pos) {
//        EnumDecoratedBlock type = state.getValue(DECORATED_TYPE);
//        return type.lightValue;
        return this.DECORATED_TYPE.lightValue;
    }
}
