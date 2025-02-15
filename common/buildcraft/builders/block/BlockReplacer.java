/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import buildcraft.builders.tile.TileReplacer;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class BlockReplacer extends BlockBCTile_Neptune<TileReplacer> implements IBlockWithFacing {
    public BlockReplacer(String idBC, BlockBehaviour.Properties properties) {
        super(idBC, properties);
    }

    @Nullable
    @Override
//    public TileBC_Neptune createTileEntity(World world, IBlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileReplacer(pos, state);
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, Player player, InteractionHand hand,
//                                    Direction side, float hitX, float hitY, float hitZ)
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!world.isClientSide) {
//            BCBuildersGuis.REPLACER.openGUI(player, pos);
            if (world.getBlockEntity(pos) instanceof TileReplacer tile) {
                MessageUtil.serverOpenTileGui(player, tile);
            }
        }
//        return true;
        return InteractionResult.SUCCESS;
    }
}
