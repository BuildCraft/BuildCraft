/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BlockFrame extends BlockBCBase_Neptune {
    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public static final VoxelShape BASE_AABB = Shapes.box(4 / 16D, 4 / 16D, 4 / 16D, 12 / 16D, 12 / 16D, 12 / 16D);
    public static final VoxelShape CONNECTION_AABB = Shapes.box(4 / 16D, 0 / 16D, 4 / 16D, 12 / 16D, 4 / 16D, 12 / 16D);

    public BlockFrame(String idBC, BlockBehaviour.Properties properties) {
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
        properties.addAll(CONNECTED_MAP.values());
    }

    @Override
    public BlockState updateShape(BlockState thisState, Direction direction, BlockState otherState, LevelAccessor world, BlockPos thisPos, BlockPos otherPos) {
        return getActualState(thisState, world, thisPos);
    }

    // public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    public BlockState getActualState(BlockState state, LevelAccessor world, BlockPos pos) {
        for (Direction side : CONNECTED_MAP.keySet()) {
            Block block = world.getBlockState(pos.relative(side)).getBlock();
            state = state.setValue(CONNECTED_MAP.get(side), block instanceof BlockFrame || block instanceof BlockQuarry);
        }
        return state;
    }

    // Calen: moved to BuildCraftBuilders#onRenderRegister
//    @Override
//    public BlockRenderLayer getBlockLayer() {
//        return BlockRenderLayer.CUTOUT;
//    }

//    @Override
//    public boolean isFullCube(IBlockState state) {
//        return false;
//    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

//    @Override
//    public boolean isOpaqueCube(IBlockState state) {
//        return false;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    // Calen ret opposite value to 1.12.2!
//    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, Direction side)
    public boolean skipRendering(BlockState thisState, BlockState otherState, Direction side) {
//        BlockState actualState = thisState.getActualState(world, pos);
        BlockState actualState = thisState;
        Direction[] facings = CONNECTED_MAP.keySet().stream()
                .filter(facing -> actualState.getValue(CONNECTED_MAP.get(facing)))
                .toArray(Direction[]::new);
        if (facings.length == 1) {
//            return side != facings[0];
            return side == facings[0];
        } else if (facings.length == 2 && facings[0] == facings[1].getOpposite()) {
//            return side != facings[0] && side != facings[1];
            return side == facings[0] || side == facings[1];
        }
//        return true;
        return false;
    }

//    @Override
//    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
//    {
//        IBlockState actualState = state.getActualState(world, pos);
//        AtomicReference<AxisAlignedBB> box = new AtomicReference<>(BASE_AABB);
//        CONNECTED_MAP.forEach((side, property) ->
//        {
//            if (actualState.getValue(property))
//            {
//                box.set(box.get().union(RotationUtil.rotateAABB(CONNECTION_AABB, side)));
//            }
//        });
//        return box.get();
//    }

    @Override
    public VoxelShape getShape(BlockState actualState, BlockGetter world, BlockPos pos, CollisionContext context) {
//        AtomicReference<AABB> box = new AtomicReference<>(BASE_AABB.bounds());
        List<VoxelShape> shapes = new ArrayList<>();
        CONNECTED_MAP.forEach((side, property) ->
        {
            if (actualState.getValue(property)) {
//                box.set(box.get().minmax(RotationUtil.rotateAABB(CONNECTION_AABB.bounds(), side)));
                shapes.add(Shapes.create(RotationUtil.rotateAABB(CONNECTION_AABB.bounds(), side)));
            }
        });
//        return Shapes.create(box.get());
        return Shapes.or(BASE_AABB, shapes.toArray(new VoxelShape[0]));
    }

//    @Override
//    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isPistonMoving)
//    {
//        IBlockState actualState = state.getActualState(world, pos);
//        addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
//        CONNECTED_MAP.keySet().stream()
//                .filter(side -> actualState.getValue(CONNECTED_MAP.get(side)))
//                .map(side -> RotationUtil.rotateAABB(CONNECTION_AABB, side))
//                .forEach(box -> addCollisionBoxToList(pos, entityBox, collidingBoxes, box));
//    }

    @Override
    public VoxelShape getCollisionShape(BlockState actualState, BlockGetter world, BlockPos pos, CollisionContext context) {
        VoxelShape shape = super.getCollisionShape(actualState, world, pos, context);
        List<VoxelShape> shapes = new ArrayList<>();
        CONNECTED_MAP.keySet().stream()
                .filter(side -> actualState.getValue(CONNECTED_MAP.get(side)))
                .map(side -> Shapes.create(RotationUtil.rotateAABB(CONNECTION_AABB.bounds(), side)))
                .forEach(box -> shapes.add(Shapes.create(box.bounds().move(pos))));
//                .forEach(box -> shape = Shapes.or(shape,));
        return Shapes.or(shape, shapes.toArray(new VoxelShape[0]));
    }


    @Override
//    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.emptyList();
    }
}
