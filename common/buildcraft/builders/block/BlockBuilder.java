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

//public class BlockBuilder extends BlockBCTile_Neptune implements IBlockWithFacing
public class BlockBuilder extends BlockBCTile_Neptune<TileBuilder> implements IBlockWithFacing, IBlockWithTickableTE<TileBuilder> {
    public static final Property<EnumOptionalSnapshotType> SNAPSHOT_TYPE = BuildCraftProperties.SNAPSHOT_TYPE;

    public BlockBuilder(String idBC, AbstractBlock.Properties properties) {
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
    public BlockState getActualState(BlockState state, IWorld world, BlockPos pos, TileEntity tile) {
        if (tile instanceof TileBuilder) {
            TileBuilder builder = (TileBuilder) tile;
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
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return new TileBuilder();
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        if (!world.isClientSide) {
//            BCBuildersGuis.BUILDER.openGUI(player, pos);
            // Calen
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof TileBuilder) {
                TileBuilder tile = (TileBuilder) te;
                MessageUtil.serverOpenTileGui(player, tile);
            }
        }
//        return true;
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean canBeRotated(IWorld world, BlockPos pos, BlockState state) {
        TileEntity tile = world.getBlockEntity(pos);
        return !(tile instanceof TileBuilder) || ((TileBuilder) tile).getBuilder() == null;
    }
}
