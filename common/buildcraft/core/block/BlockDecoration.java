/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;

import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.lib.block.BlockBCBase_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockDecoration extends BlockBCBase_Neptune {
    // Calen: create different blocks instead of meta/blockState
//    public static final Property<EnumDecoratedBlock> DECORATED_TYPE = BuildCraftProperties.DECORATED_BLOCK;
    public final EnumDecoratedBlock DECORATED_TYPE;

    public BlockDecoration(String idBC, BlockBehaviour.Properties properties, EnumDecoratedBlock decoratedType) {
//        super(Material.IRON, id);
        super(idBC, properties);
//        setDefaultState(getDefaultState().withProperty(DECORATED_TYPE, EnumDecoratedBlock.DESTROY));
        this.DECORATED_TYPE = decoratedType;
    }

    // IBlockState

//    @Override
////    protected BlockStateContainer createBlockState()
//    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
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
//    public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> list) {
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
//    public int getLightValue(IBlockState state, LevelAccessor world, BlockPos pos)
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
//        EnumDecoratedBlock type = state.getValue(DECORATED_TYPE);
//        return type.lightValue;
        return this.DECORATED_TYPE.lightValue;
    }
}
