/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.factory.tile.TileAutoWorkbenchFluids;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.misc.MessageUtil;
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

public class BlockAutoWorkbenchFluids extends BlockBCTile_Neptune<TileAutoWorkbenchFluids> implements IBlockWithTickableTE<TileAutoWorkbenchFluids> {

    public BlockAutoWorkbenchFluids(String idBC, AbstractBlock.Properties props) {
        super(idBC, props);
    }

    @Override
//    public TileBC_Neptune createTileEntity(World world, IBlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return new TileAutoWorkbenchFluids();
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        if (!world.isClientSide) {
            // TODO Calen tile impl INamedContainerProvider
//            BCFactoryGuis.AUTO_WORKBENCH_FLUIDS.openGUI(player, pos);
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof IBCTileMenuProvider) {
                IBCTileMenuProvider tile = (IBCTileMenuProvider) te;
                MessageUtil.serverOpenTileGui(player, tile, pos);
            }
        }
//        return true;
        return ActionResultType.SUCCESS;
    }
}
