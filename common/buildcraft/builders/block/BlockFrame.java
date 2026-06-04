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
import buildcraft.lib.compat.MaterialBC;
import net.minecraft.block.AbstractBlock;
import net.minecraft.state.property.Property;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.RotationUtil;

public class BlockFrame extends BlockBCBase_Neptune {
    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public static final Box BASE_AABB = new Box(4 / 16D, 4 / 16D, 4 / 16D, 12 / 16D, 12 / 16D, 12 / 16D);
    public static final Box CONNECTION_AABB = new Box(4 / 16D, 0 / 16D, 4 / 16D, 12 / 16D, 4 / 16D, 12 / 16D);

    public BlockFrame(AbstractBlock.Settings material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public BlockState getActualState(BlockState state, BlockView world, BlockPos pos) {
        for (Direction side : CONNECTED_MAP.keySet()) {
            Block block = world.getBlockState(pos.offset(side)).getBlock();
            state = state.with(CONNECTED_MAP.get(side), block instanceof BlockFrame || block instanceof BlockQuarry);
        }
        return state;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public RenderLayer getBlockLayer() {
        return RenderLayer.getCutout();
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean isFullCube(BlockState state) {
        return false;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    // STUB(R.Chen): shouldSideBeRendered, getBoundingBox, addCollisionBoxToList removed in 1.20.1

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public List<ItemStack> getDrops(BlockView world, BlockPos pos, BlockState state, int fortune) {
        return Collections.emptyList();
    }
}
