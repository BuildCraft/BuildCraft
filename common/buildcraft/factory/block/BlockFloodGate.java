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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockFloodGate extends BlockBCTile_Neptune<TileFloodGate> implements IBlockWithTickableTE<TileFloodGate> {
    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP;

    static {
        CONNECTED_MAP = new HashMap<>(BuildCraftProperties.CONNECTED_MAP);
        CONNECTED_MAP.remove(Direction.UP);
    }

    public BlockFloodGate(String idBC, BlockBehaviour.Properties props) {
        super(idBC, props);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    @Override
//    public TileBC_Neptune createTileEntity(World world, IBlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileFloodGate(pos, state);
    }

    @Override
    public BlockState getActualState(BlockState state, LevelAccessor world, BlockPos pos, BlockEntity tile) {
        if (tile instanceof TileFloodGate floodGate) {
            for (Direction side : CONNECTED_MAP.keySet()) {
                state = state.setValue(CONNECTED_MAP.get(side), floodGate.openSides.contains(side));
            }
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState thisState, Direction facing, BlockState otherState, LevelAccessor world, BlockPos thisPos, BlockPos otherPos) {
        BlockEntity tile = world.getBlockEntity(thisPos);
        thisState = super.updateShape(thisState, facing, otherState, world, thisPos, otherPos);
        return getActualState(thisState, world, thisPos, tile);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return getActualState(state, world, pos, world.getBlockEntity(pos));
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ)
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction side = hitResult.getDirection();

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof IToolWrench) {
            if (!world.isClientSide) {
                if (side != Direction.UP) {
                    BlockEntity tile = world.getBlockEntity(pos);
                    if (tile instanceof TileFloodGate) {
                        if (CONNECTED_MAP.containsKey(side)) {
                            TileFloodGate floodGate = (TileFloodGate) tile;
                            if (!floodGate.openSides.remove(side)) {
                                floodGate.openSides.add(side);
                            }
                            floodGate.queue.clear();
                            floodGate.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
//                            return true;
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
//            return false;
            return InteractionResult.FAIL;
        }
//        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
        return super.use(state, world, pos, player, hand, hitResult);
    }
}
