/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.block;

import buildcraft.builders.tile.TileElectronicLibrary;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
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

//public class BlockElectronicLibrary extends BlockBCTile_Neptune implements IBlockWithFacing
public class BlockElectronicLibrary extends BlockBCTile_Neptune<TileElectronicLibrary> implements IBlockWithFacing, IBlockWithTickableTE<TileElectronicLibrary> {
    public BlockElectronicLibrary(String idBC, AbstractBlock.Properties properties) {
        super(idBC, properties);
    }

    @Override
//    public TileBC_Neptune createTileEntity(World world, IBlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return new TileElectronicLibrary();
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, PlayerEntity player, Hand hand,
//                                    Direction side, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        if (!world.isClientSide) {
//            BCBuildersGuis.LIBRARY.openGUI(player, pos);
            // Calen
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof TileElectronicLibrary) {
                TileElectronicLibrary tile = (TileElectronicLibrary) te;
                MessageUtil.serverOpenTileGui(player, tile);
            }
        }
//        return true;
        return ActionResultType.SUCCESS;
    }
}
