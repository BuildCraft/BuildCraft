/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.core.block;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import buildcraft.api.enums.EnumSpring;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.data.XorShift128Random;
import net.minecraft.sound.BlockSoundGroup;

public class BlockSpring extends BlockBCBase_Neptune implements BlockEntityProvider {
    public static final Property<EnumSpring> SPRING_TYPE = BuildCraftProperties.SPRING_TYPE;

    public static final XorShift128Random rand = new XorShift128Random();

    public BlockSpring(String id) {
        super(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(-1.0F, 6000000.0F).sounds(BlockSoundGroup.STONE), id);
        setDefaultState(getDefaultState().with(SPRING_TYPE, EnumSpring.WATER));
    }

    // BlockState

    @Override
    protected void addProperties(List<Property<?>> properties) {
        properties.add(SPRING_TYPE);
    }

    // Block entity

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        Supplier<BlockEntity> constructor = state.get(SPRING_TYPE).tileConstructor;
        return constructor != null ? constructor.get() : null;
    }

    // Tick

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        generateSpringBlock(world, pos, state);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        world.scheduleBlockTick(pos, this, state.get(SPRING_TYPE).tickRate);
    }

    private void generateSpringBlock(World world, BlockPos pos, BlockState state) {
        EnumSpring spring = state.get(SPRING_TYPE);
        world.scheduleBlockTick(pos, this, spring.tickRate);
        if (!spring.canGen || spring.liquidBlock == null) {
            return;
        }
        if (!world.isAir(pos.up())) {
            return;
        }
        if (spring.chance != -1 && rand.nextInt(spring.chance) != 0) {
            return;
        }
        world.setBlockState(pos.up(), spring.liquidBlock);
    }
}
