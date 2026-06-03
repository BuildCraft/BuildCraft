/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.sound.BlockSoundGroup;
import buildcraft.lib.compat.MaterialBC;
import net.minecraft.block.AbstractBlock;
import net.minecraft.state.property.Property;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.tile.TileQuarry;

public class BlockQuarry extends BlockBCTile_Neptune implements IBlockWithFacing {
    private static final Identifier ADVANCEMENT = new Identifier("buildcraftbuilders:shaping_the_world");

    public BlockQuarry(AbstractBlock.Settings material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(BuildCraftProperties.CONNECTED_MAP.values());
    }

    private boolean isConnected(BlockView world, BlockPos pos, BlockState state, Direction side) {
        Direction facing = side;
        if (Arrays.asList(Direction.HORIZONTALS).contains(facing)) {
            facing = Direction.fromHorizontal(
                side.getHorizontal() + 2 + state.get(getFacingProperty()).getHorizontal());
        }
        BlockEntity tile = world.getBlockEntity(pos.offset(facing));
        return tile != null && CapUtil.hasCapability(tile, CapUtil.CAP_ITEMS, facing.getOpposite());
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public BlockState getActualState(BlockState state, BlockView world, BlockPos pos) {
        for (Direction face : Direction.values()) {
            state =
                state.with(BuildCraftProperties.CONNECTED_MAP.get(face), isConnected(world, pos, state, face));
        }
        return state;
    }

    @Override
    public TileBC_Neptune createTileEntity(World world, BlockState state) {
        return new TileQuarry();
    }

    @Override
    public boolean canBeRotated(World world, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, BlockState state) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileQuarry) {
            for (BlockPos blockPos : ((TileQuarry) tile).framePoses) {
                if (world.getBlockState(blockPos).getBlock() == BCBuildersBlocks.frame) {
                    world.setBlockState(blockPos);
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public BlockSoundGroup getSoundType(BlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        return BlockSoundGroup.ANVIL;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer,
        ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (placer instanceof PlayerEntity) {
            AdvancementUtil.unlockAdvancement((PlayerEntity) placer, ADVANCEMENT);
        }
    }
}
