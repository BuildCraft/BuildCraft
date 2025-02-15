/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import buildcraft.builders.tile.TileFiller;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
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

public class BlockFiller extends BlockBCTile_Neptune<TileFiller> implements IBlockWithFacing {
    // public static final IProperty<EnumFillerPattern> PATTERN = BuildCraftProperties.FILLER_PATTERN;

    // public BlockFiller(Material material, String id)
    public BlockFiller(String idBC, BlockBehaviour.Properties properties) {
        super(idBC, properties);
        // setDefaultState(getDefaultState().withProperty(PATTERN, EnumFillerPattern.NONE));
    }

    // BlockState

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        // properties.add(PATTERN);
    }

    @Override
    public BlockState getActualState(BlockState state, LevelAccessor world, BlockPos pos, BlockEntity tile) {
        if (tile instanceof TileFiller filler) {
            // return state.withProperty(PATTERN, EnumFillerPattern.NONE); // FIXME
        }
        return state;
    }
    // Others

    @Override
//    public TileBC_Neptune createTileEntity(World world, IBlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileFiller(pos, state);
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ)
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileFiller filler) {
            if (!filler.hasBox()) {
//                return false;
                return InteractionResult.FAIL;
            }
            if (!world.isClientSide) {
//            BCBuildersGuis.FILLER.openGUI(player, pos);
                MessageUtil.serverOpenTileGui(player, filler);
            }
        }
//        return true;
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canBeRotated(LevelAccessor world, BlockPos pos, BlockState state) {
        return false;
    }
}
