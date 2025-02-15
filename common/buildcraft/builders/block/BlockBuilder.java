/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.block;

import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

//public class BlockBuilder extends BlockBCTile_Neptune implements IBlockWithFacing
public class BlockBuilder extends BlockBCTile_Neptune<TileBuilder> implements IBlockWithFacing, IBlockWithTickableTE<TileBuilder> {
    public static final Property<EnumOptionalSnapshotType> SNAPSHOT_TYPE = BuildCraftProperties.SNAPSHOT_TYPE;

    public BlockBuilder(String idBC, BlockBehaviour.Properties properties) {
        super(idBC, properties);
//        setDefaultState(getDefaultState().withProperty(SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE));
        registerDefaultState(
                defaultBlockState()
                        .setValue(SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE)
        );
    }

    // BlockState

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(SNAPSHOT_TYPE);
    }

    @Override
    public BlockState getActualState(BlockState state, LevelAccessor world, BlockPos pos, BlockEntity tile) {
        if (tile instanceof TileBuilder builder) {
            return state
                    .setValue(
                            SNAPSHOT_TYPE,
                            EnumOptionalSnapshotType.fromNullable(builder.snapshotType)
                    );
        }
        return state;
    }

    // Others

    @Override
//    public TileBC_Neptune createTileEntity(World world, IBlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileBuilder(pos, state);
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ)
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!world.isClientSide) {
//            BCBuildersGuis.BUILDER.openGUI(player, pos);
            // Calen
            if (world.getBlockEntity(pos) instanceof TileBuilder tile) {
                MessageUtil.serverOpenTileGui(player, tile);
            }
        }
//        return true;
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canBeRotated(LevelAccessor world, BlockPos pos, BlockState state) {
        BlockEntity tile = world.getBlockEntity(pos);
        return !(tile instanceof TileBuilder) || ((TileBuilder) tile).getBuilder() == null;
    }
}
