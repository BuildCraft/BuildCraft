/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.RotationUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BlockFrame extends BlockBCBase_Neptune {
    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public static final VoxelShape BASE_AABB = VoxelShapes.box(4 / 16D, 4 / 16D, 4 / 16D, 12 / 16D, 12 / 16D, 12 / 16D);
    public static final VoxelShape CONNECTION_AABB = VoxelShapes.box(4 / 16D, 0 / 16D, 4 / 16D, 12 / 16D, 4 / 16D, 12 / 16D);

    public BlockFrame(String idBC, AbstractBlock.Properties properties) {
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
    public BlockState updateShape(BlockState thisState, Direction direction, BlockState otherState, IWorld world, BlockPos thisPos, BlockPos otherPos) {
        return getActualState(thisState, world, thisPos);
    }

    // public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    public BlockState getActualState(BlockState state, IWorld world, BlockPos pos) {
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

//    @Override
//    public boolean isOpaqueCube(IBlockState state) {
//        return false;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, IBlockReader world, BlockPos pos) {
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
    public VoxelShape getShape(BlockState actualState, IBlockReader world, BlockPos pos, ISelectionContext context) {
//        AtomicReference<AxisAlignedBB> box = new AtomicReference<>(BASE_AABB.bounds());
        List<VoxelShape> shapes = new ArrayList<>();
        CONNECTED_MAP.forEach((side, property) ->
        {
            if (actualState.getValue(property)) {
//                box.set(box.get().minmax(RotationUtil.rotateAABB(CONNECTION_AABB.bounds(), side)));
                shapes.add(VoxelShapes.create(RotationUtil.rotateAABB(CONNECTION_AABB.bounds(), side)));
            }
        });
//        return VoxelShapes.create(box.get());
        return VoxelShapes.or(BASE_AABB, shapes.toArray(new VoxelShape[0]));
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
    public VoxelShape getCollisionShape(BlockState actualState, IBlockReader world, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = super.getCollisionShape(actualState, world, pos, context);
        List<VoxelShape> shapes = new ArrayList<>();
        CONNECTED_MAP.keySet().stream()
                .filter(side -> actualState.getValue(CONNECTED_MAP.get(side)))
                .map(side -> VoxelShapes.create(RotationUtil.rotateAABB(CONNECTION_AABB.bounds(), side)))
                .forEach(box -> shapes.add(VoxelShapes.create(box.bounds().move(pos))));
//                .forEach(box -> shape = VoxelShapes.or(shape,));
        return VoxelShapes.or(shape, shapes.toArray(new VoxelShape[0]));
    }


    @Override
//    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.emptyList();
    }
}
