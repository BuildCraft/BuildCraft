/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tools.IToolWrench;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockFloodGate extends BlockBCTile_Neptune<TileFloodGate> implements IBlockWithTickableTE<TileFloodGate> {
    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP;

    static {
        CONNECTED_MAP = new HashMap<>(BuildCraftProperties.CONNECTED_MAP);
        CONNECTED_MAP.remove(Direction.UP);
    }

    public BlockFloodGate(String idBC, AbstractBlock.Properties props) {
        super(idBC, props);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    @Override
//    public TileBC_Neptune createTileEntity(World world, IBlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return new TileFloodGate();
    }

    @Override
    public BlockState getActualState(BlockState state, IWorld world, BlockPos pos, TileEntity tile) {
        if (tile instanceof TileFloodGate) {
            TileFloodGate floodGate = (TileFloodGate) tile;
            for (Direction side : CONNECTED_MAP.keySet()) {
                state = state.setValue(CONNECTED_MAP.get(side), floodGate.openSides.contains(side));
            }
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState thisState, Direction facing, BlockState otherState, IWorld world, BlockPos thisPos, BlockPos otherPos) {
        TileEntity tile = world.getBlockEntity(thisPos);
        thisState = super.updateShape(thisState, facing, otherState, world, thisPos, otherPos);
        return getActualState(thisState, world, thisPos, tile);
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return getActualState(state, world, pos, world.getBlockEntity(pos));
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        Direction side = hitResult.getDirection();

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof IToolWrench) {
            if (!world.isClientSide) {
                if (side != Direction.UP) {
                    TileEntity tile = world.getBlockEntity(pos);
                    if (tile instanceof TileFloodGate) {
                        if (CONNECTED_MAP.containsKey(side)) {
                            TileFloodGate floodGate = (TileFloodGate) tile;
                            if (!floodGate.openSides.remove(side)) {
                                floodGate.openSides.add(side);
                            }
                            floodGate.queue.clear();
                            floodGate.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
//                            return true;
                            return ActionResultType.SUCCESS;
                        }
                    }
                }
            }
//            return false;
            return ActionResultType.FAIL;
        }
//        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
        return super.use(state, world, pos, player, hand, hitResult);
    }
}
