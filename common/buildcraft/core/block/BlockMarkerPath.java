/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;

import buildcraft.core.tile.TileMarkerPath;
import buildcraft.lib.block.BlockMarkerBase;
import buildcraft.lib.misc.PermissionUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockMarkerPath extends BlockMarkerBase {
    public BlockMarkerPath(String idBC, AbstractBlock.Properties properties) {
        super(idBC, properties);
    }

    @Override
//    public TileBC_Neptune createTileEntity(World worldIn, IBlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return new TileMarkerPath();
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        if (!world.isClientSide) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileMarkerPath) {
                TileMarkerPath marker = (TileMarkerPath) tile;
                if (PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, player, marker.getPermBlock())) {
                    marker.reverseDirection();
                }
            }
        }
//        return true;
        return ActionResultType.SUCCESS;
    }
}
