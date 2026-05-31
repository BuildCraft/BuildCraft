/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.state.property.Property;
import net.minecraft.state.StateManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.tile.TileMarker;

public abstract class BlockMarkerBase extends BlockBCTile_Neptune implements ICustomRotationHandler {
    private static final Map<Direction, Box> BOUNDING_BOXES = new EnumMap<>(Direction.class);

    static {
        double halfWidth = 0.1;
        double h = 0.65;
        // Little variables to make reading a *bit* more sane
        final double nw = 0.5 - halfWidth;
        final double pw = 0.5 + halfWidth;
        final double ih = 1 - h;
        BOUNDING_BOXES.put(Direction.DOWN, new Box(nw, ih, nw, pw, 1, pw));
        BOUNDING_BOXES.put(Direction.UP, new Box(nw, 0, nw, pw, h, pw));
        BOUNDING_BOXES.put(Direction.SOUTH, new Box(nw, nw, 0, pw, pw, h));
        BOUNDING_BOXES.put(Direction.NORTH, new Box(nw, nw, ih, pw, pw, 1));
        BOUNDING_BOXES.put(Direction.EAST, new Box(0, nw, nw, h, pw, pw));
        BOUNDING_BOXES.put(Direction.WEST, new Box(ih, nw, nw, 1, pw, pw));
    }

    public BlockMarkerBase(Material material, String id) {
        super(material, id);
        setHardness(0.25f);

        BlockState defaultState = getDefaultState();
        defaultState = defaultState.withProperty(BuildCraftProperties.BLOCK_FACING_6, Direction.UP);
        defaultState = defaultState.withProperty(BuildCraftProperties.ACTIVE, false);
        setDefaultState(defaultState);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BuildCraftProperties.BLOCK_FACING_6, BuildCraftProperties.ACTIVE);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(BuildCraftProperties.BLOCK_FACING_6).getIndex();
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BuildCraftProperties.BLOCK_FACING_6, Direction.getFront(meta));
    }

    @Override
    public BlockState getActualState(BlockState state, BlockView world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMarker) {
            TileMarker<?> marker = (TileMarker<?>) tile;
            state = state.withProperty(BuildCraftProperties.ACTIVE, marker.isActiveForRender());
        }
        return state;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public RenderLayer getBlockLayer() {
        return RenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public Box getCollisionBoundingBox(BlockState state, BlockView world, BlockPos pos) {
        return null;
    }

    @Override
    public Box getBoundingBox(BlockState state, BlockView source, BlockPos pos) {
        return BOUNDING_BOXES.get(state.getValue(BuildCraftProperties.BLOCK_FACING_6));
    }
    
    @Override
    public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, Hand hand) {
        BlockState state = getDefaultState();
        state = state.withProperty(BuildCraftProperties.BLOCK_FACING_6, facing);
        return state;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, Direction side) {
        return world.isSideSolid(pos.offset(side.getOpposite()), side);
    }
    
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (state.getBlock() != this) {
            return;
        }
        Direction sideOn = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
        if (!canPlaceBlockOnSide(world, pos, sideOn)) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public ActionResult attemptRotation(World world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof BlockMarkerBase) {// Just check to make sure we have the right block...
            Property<Direction> prop = BuildCraftProperties.BLOCK_FACING_6;
            return VanillaRotationHandlers.rotateEnumFacing(world, pos, state, prop, VanillaRotationHandlers.ROTATE_FACING);
        } else {
            return ActionResult.PASS;
        }
    }
}
