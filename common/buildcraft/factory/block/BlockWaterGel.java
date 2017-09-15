/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.SoundUtil;

import buildcraft.factory.BCFactoryItems;

public class BlockWaterGel extends BlockBCBase_Neptune {
    public enum GelStage implements IStringSerializable {
        SPREAD_0(0.3f, true, 3),
        SPREAD_1(0.4f, true, 3),
        SPREAD_2(0.6f, true, 3),
        SPREAD_3(0.8f, true, 3),
        GELLING_0(1.0f, false, 0.6f),
        GELLING_1(1.2f, false, 0.6f),
        GEL(1.5f, false, 0.1f);

        public static final GelStage[] VALUES = values();

        public final SoundType soundType;
        public final String modelName = name().toLowerCase(Locale.ROOT);
        public final boolean spreading;
        public final float hardness;

        GelStage(float pitch, boolean spreading, float hardness) {
            this.soundType = new SoundType(//
                    SoundType.SLIME.volume,//
                    pitch,//
                    SoundEvents.BLOCK_SLIME_BREAK,//
                    SoundEvents.BLOCK_SLIME_STEP,//
                    SoundEvents.BLOCK_SLIME_PLACE,//
                    SoundEvents.BLOCK_SLIME_HIT,//
                    SoundEvents.BLOCK_SLIME_FALL//
            );
            this.spreading = spreading;
            this.hardness = hardness;
        }

        @Override
        public String getName() {
            return modelName;
        }

        public static GelStage fromMeta(int meta) {
            if (meta < 0) {
                return GEL;
            }
            return VALUES[meta % VALUES.length];
        }

        public int getMeta() {
            return ordinal();
        }

        public GelStage next() {
            if (this == SPREAD_0) return SPREAD_1;
            if (this == SPREAD_1) return SPREAD_2;
            if (this == SPREAD_2) return SPREAD_3;
            if (this == SPREAD_3) return GELLING_0;
            if (this == GELLING_0) return GELLING_1;
            return GEL;
        }
    }

    public static final PropertyEnum<GelStage> PROP_STAGE = PropertyEnum.create("stage", GelStage.class);

    public BlockWaterGel(Material material, String id) {
        super(material, id);
        setSoundType(SoundType.SLIME);
    }

    // BlockState

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PROP_STAGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(PROP_STAGE, GelStage.fromMeta(meta & 7));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PROP_STAGE).getMeta();
    }

    // Logic

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        GelStage stage = state.getValue(PROP_STAGE);
        GelStage next = stage.next();
        IBlockState nextState = state.withProperty(PROP_STAGE, next);
        if (stage.spreading) {
            List<BlockPos> openSet = new ArrayList<>();
            Set<BlockPos> closedSet = new HashSet<>();
            List<BlockPos> changeable = new ArrayList<>();
            List<EnumFacing> faces = new ArrayList<>();
            Collections.addAll(faces, EnumFacing.VALUES);
            Collections.shuffle(faces);
            for (EnumFacing face : faces) {
                openSet.add(pos.offset(face));
            }
            Collections.shuffle(faces);
            int tries = 0;

            while (openSet.size() > 0 && changeable.size() < 3 && tries < 10_000) {
                openSet.sort(Comparator.comparingDouble(a -> a.distanceSq(pos)));

                BlockPos test = openSet.remove(0);
                closedSet.add(test);

                boolean water = isWater(world, test);
                boolean spreadable = water || canSpread(world, test);

                if (water && world.getBlockState(test).getValue(BlockLiquid.LEVEL) == 0) {
                    changeable.add(test);
                }
                if (spreadable) {
                    Collections.shuffle(faces);
                    for (EnumFacing face : faces) {
                        BlockPos n = test.offset(face);
                        if (!closedSet.contains(n) && !openSet.contains(n)) {
                            openSet.add(n);
                        }
                    }
                }
                tries++;
            }
            final int time = next.spreading ? 200 : 400;
            if (changeable.size() == 3 || world.rand.nextDouble() < 0.5) {
                for (BlockPos p : changeable) {
                    world.setBlockState(p, nextState);
                    world.scheduleUpdate(p, this, rand.nextInt(150) + time);
                }
                world.setBlockState(pos, nextState);
                SoundUtil.playBlockPlace(world, pos);
            }
            world.scheduleUpdate(pos, this, rand.nextInt(150) + time);
        } else if (stage != next) {
            if (notTouchingWater(world, pos)) {
                world.setBlockState(pos, nextState);
                world.scheduleUpdate(pos, this, rand.nextInt(150) + 400);
            } else {
                world.scheduleUpdate(pos, this, rand.nextInt(150) + 600);
            }
        }
    }

    private static boolean notTouchingWater(World world, BlockPos pos) {
        for (EnumFacing face : EnumFacing.VALUES) {
            if (isWater(world, pos.offset(face))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWater(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() == Blocks.WATER;
    }

    private boolean canSpread(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this) {
            return true;
        }
        return false;
    }

    // Misc

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity) {
        GelStage stage = state.getValue(PROP_STAGE);
        return stage.soundType;
    }

    @Override
    public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
        GelStage stage = state.getValue(PROP_STAGE);
        return stage.hardness;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return BCFactoryItems.gelledWater;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        GelStage stage = state.getValue(PROP_STAGE);
        if (stage.spreading) {
            return random.nextInt(2) + 1;
        } else {
            return 1;
        }
    }
}
