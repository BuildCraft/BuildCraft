/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.builders.tile.TileArchitectTable;
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

//public class BlockArchitectTable extends BlockBCTile_Neptune implements IBlockWithFacing
public class BlockArchitectTable extends BlockBCTile_Neptune<TileArchitectTable> implements IBlockWithFacing, IBlockWithTickableTE<TileArchitectTable> {
    public static final Property<Boolean> PROP_VALID = BuildCraftProperties.VALID;

//    private static final int META_VALID_INDEX = 4;

    public BlockArchitectTable(String idBC, AbstractBlock.Properties properties) {
        super(idBC, properties);
//        setDefaultState(getDefaultState().withProperty(PROP_VALID, Boolean.TRUE));
        registerDefaultState(
                defaultBlockState()
                        .setValue(PROP_VALID, Boolean.TRUE)
        );
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(PROP_VALID);
    }

//    @Override
//    public IBlockState getStateFromMeta(int meta) {
//        IBlockState state = super.getStateFromMeta(meta);
//        state = state.withProperty(PROP_VALID, (meta & META_VALID_INDEX) == 0);
//        return state;
//    }

//    @Override
//    public int getMetaFromState(IBlockState state) {
//        return super.getMetaFromState(state) | (state.getValue(PROP_VALID) ? 0 : META_VALID_INDEX);
//    }

    @Override
//    public TileBC_Neptune createTileEntity(World world, IBlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
//        return new TileArchitectTable();
        return new TileArchitectTable();
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        if (!world.isClientSide) {
//            BCBuildersGuis.ARCHITECT.openGUI(player, pos);
            // Calen
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof TileArchitectTable) {
                TileArchitectTable tile = (TileArchitectTable) te;
                MessageUtil.serverOpenTileGui(player, tile);
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
