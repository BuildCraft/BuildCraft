/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;
import java.util.List;

public class BlockQuarry extends BlockBCTile_Neptune<TileQuarry> implements IBlockWithFacing, IBlockWithTickableTE<TileQuarry> {
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftbuilders:shaping_the_world");

    public BlockQuarry(String idBC, BlockBehaviour.Properties properties) {
        super(idBC, properties);
        this.registerDefaultState(
                this.getStateDefinition().any()
                        .setValue(BuildCraftProperties.CONNECTED_UP, false)
                        .setValue(BuildCraftProperties.CONNECTED_DOWN, false)
                        .setValue(BuildCraftProperties.CONNECTED_EAST, false)
                        .setValue(BuildCraftProperties.CONNECTED_WEST, false)
                        .setValue(BuildCraftProperties.CONNECTED_NORTH, false)
                        .setValue(BuildCraftProperties.CONNECTED_SOUTH, false)
        );
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(BuildCraftProperties.CONNECTED_MAP.values());
    }

    // private boolean isConnected(IBlockAccess world, BlockPos pos, IBlockState state, Direction side)
    private boolean isConnected(LevelAccessor world, BlockPos pos, BlockState state, Direction side) {
        Direction facing = side;
//        if (Arrays.asList(Direction.HORIZONTALS).contains(facing))
        if (Arrays.asList(Direction.BY_2D_DATA).contains(facing)) {
//            facing = Direction.getHorizontal(
            facing = Direction.from2DDataValue(
//                    side.getHorizontalIndex() + 2 + state.getValue(getFacingProperty()).getHorizontalIndex()
                    side.get2DDataValue() + 2 + state.getValue(getFacingProperty()).get2DDataValue()
            );
        }
        BlockEntity tile = world.getBlockEntity(pos.relative(facing));
//        return tile != null && tile.hasCapability(CapUtil.CAP_ITEMS, facing.getOpposite());
        return tile != null && tile.getCapability(CapUtil.CAP_ITEMS, facing.getOpposite()).isPresent();
    }

    @Override
    public BlockState updateShape(BlockState thisState, Direction direction, BlockState otherState, LevelAccessor world, BlockPos thisPos, BlockPos otherPos) {
        return getActualState(thisState, world, thisPos, null);
    }

    @Override
    public BlockState getActualState(BlockState state, LevelAccessor world, BlockPos pos, BlockEntity tile) {
        for (Direction face : Direction.VALUES) {
            state =
                    state.setValue(BuildCraftProperties.CONNECTED_MAP.get(face), isConnected(world, pos, state, face));
        }
        return state;
    }

    @Override
//    public TileBC_Neptune createTileEntity(World world, IBlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
//        return new TileQuarry();
        return new TileQuarry(pos, state);
    }

    @Override
    public boolean canBeRotated(LevelAccessor world, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
//    public void breakBlock(World world, BlockPos pos, IBlockState state)
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        // Calen: onRemove will be called when chest/shulkerbox placed nearby, without newState.getBlock() == state.getBlock(), the frame will be rebuilt
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileQuarry quarry) {
            for (BlockPos blockPos : quarry.framePoses) {
                if (world.getBlockState(blockPos).getBlock() == BCBuildersBlocks.frame.get()) {
                    world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
//        super.breakBlock(world, pos, state);
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
//    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity)
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @org.jetbrains.annotations.Nullable Entity entity) {
        return SoundType.ANVIL;
    }

    @Override
//    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
//        super.onBlockPlacedBy(world, pos, state, placer, stack);
        super.setPlacedBy(world, pos, state, placer, stack);
        if (placer instanceof Player) {
            AdvancementUtil.unlockAdvancement((Player) placer, ADVANCEMENT);
        }
    }

    // Calen: selected shape
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        VoxelShape shape = super.getShape(state, world, pos, context);
//        if (world.getBlockEntity(pos) instanceof TileQuarry tile)
//        {
//            return Shapes.or(shape, tile.getCollisionBoxes().toArray(new VoxelShape[0]));
//        }
        return shape;
//        return Block.box(-5, 0, -5, 20, 10, 20);
    }

    // Calen: from BCBuildersEventDist#onGetCollisionBoxesForQuarry
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        VoxelShape shape = super.getInteractionShape(state, world, pos);
        if (world.getBlockEntity(pos) instanceof TileQuarry tile) {
            return Shapes.or(shape, tile.getCollisionBoxes().toArray(new VoxelShape[0]));
        }
        return shape;
    }
}
