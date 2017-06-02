/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.RotationUtil;

public class BlockFrame extends BlockBCBase_Neptune {
    public static final Map<EnumFacing, IProperty<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(4 / 16D, 4 / 16D, 4 / 16D, 12 / 16D, 12 / 16D, 12 / 16D);
    public static final AxisAlignedBB CONNECTION_AABB = new AxisAlignedBB(4 / 16D, 0 / 16D, 4 / 16D, 12 / 16D, 4 / 16D, 12 / 16D);

    public BlockFrame(Material material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        for (EnumFacing side : CONNECTED_MAP.keySet()) {
            Block block = world.getBlockState(pos.offset(side)).getBlock();
            state = state.withProperty(CONNECTED_MAP.get(side), block instanceof BlockFrame || block instanceof BlockQuarry);
        }
        return state;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        IBlockState actualState = state.getActualState(world, pos);
        EnumFacing[] facings = CONNECTED_MAP.keySet().stream()
                .filter(facing -> actualState.getValue(CONNECTED_MAP.get(facing)))
                .toArray(EnumFacing[]::new);
        if (facings.length == 1) {
            return side != facings[0];
        } else if (facings.length == 2 && facings[0] == facings[1].getOpposite()) {
            return side != facings[0] && side != facings[1];
        }
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        IBlockState actualState = state.getActualState(world, pos);
        AtomicReference<AxisAlignedBB> box = new AtomicReference<>(BASE_AABB);
        CONNECTED_MAP.forEach((side, property) -> {
            if (actualState.getValue(property)) {
                box.set(box.get().union(RotationUtil.rotateAABB(CONNECTION_AABB, side)));
            }
        });
        return box.get();
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isPistonMoving) {
        IBlockState actualState = state.getActualState(world, pos);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
        CONNECTED_MAP.keySet().stream()
                .filter(side -> actualState.getValue(CONNECTED_MAP.get(side)))
                .map(side -> RotationUtil.rotateAABB(CONNECTION_AABB, side))
                .forEach(box -> addCollisionBoxToList(pos, entityBox, collidingBoxes, box));
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return Collections.emptyList();
    }
}
