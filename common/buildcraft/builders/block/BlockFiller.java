/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import buildcraft.builders.tile.TileFiller;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.List;

public class BlockFiller extends BlockBCTile_Neptune<TileFiller> implements IBlockWithFacing, IBlockWithTickableTE<TileFiller> {
    // public static final IProperty<EnumFillerPattern> PATTERN = BuildCraftProperties.FILLER_PATTERN;

    // public BlockFiller(Material material, String id)
    public BlockFiller(String idBC, AbstractBlock.Properties properties) {
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
    public BlockState getActualState(BlockState state, IWorld world, BlockPos pos, TileEntity tile) {
        if (tile instanceof TileFiller) {
            // return state.withProperty(PATTERN, EnumFillerPattern.NONE); // FIXME
        }
        return state;
    }
    // Others

    @Override
//    public TileBC_Neptune createTileEntity(World world, IBlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return new TileFiller();
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileFiller) {
            TileFiller filler = (TileFiller) tile;
            if (!filler.hasBox()) {
//                return false;
                return ActionResultType.FAIL;
            }
            if (!world.isClientSide) {
//            BCBuildersGuis.FILLER.openGUI(player, pos);
                MessageUtil.serverOpenTileGui(player, filler);
            }
        }
//        return true;
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean canBeRotated(IWorld world, BlockPos pos, BlockState state) {
        return false;
    }
}
