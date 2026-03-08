/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import java.util.*;

public class BlockWaterGel extends BlockBCBase_Neptune {
    // public enum GelStage implements IStringSerializable
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
                    SoundType.SLIME_BLOCK.volume, //
                    pitch, //
                    SoundEvents.SLIME_BLOCK_BREAK, //
                    SoundEvents.SLIME_BLOCK_STEP, //
                    SoundEvents.SLIME_BLOCK_PLACE, //
                    SoundEvents.SLIME_BLOCK_HIT, //
                    SoundEvents.SLIME_BLOCK_FALL//
            );
            this.spreading = spreading;
            this.hardness = hardness;
        }

        @Override
//        public String getName()
        public String getSerializedName() {
            return modelName;
        }

//        public static GelStage fromMeta(int meta) {
//            if (meta < 0) {
//                return GEL;
//            }
//            return VALUES[meta % VALUES.length];
//        }

//        public int getMeta() {
//            return ordinal();
//        }

        public GelStage next() {
            if (this == SPREAD_0) return SPREAD_1;
            if (this == SPREAD_1) return SPREAD_2;
            if (this == SPREAD_2) return SPREAD_3;
            if (this == SPREAD_3) return GELLING_0;
            if (this == GELLING_0) return GELLING_1;
            return GEL;
        }
    }

    public static final EnumProperty<GelStage> PROP_STAGE = EnumProperty.create("stage", GelStage.class);

    public BlockWaterGel(String idBC, Properties props) {
        super(idBC, props);
//        setSoundType(SoundType.SLIME_BLOCK);
    }

    // BlockState

    @Override
//    protected BlockStateContainer createBlockState()
    protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PROP_STAGE);
    }

//    @Override
//    public IBlockState getStateFromMeta(int meta) {
//        return getDefaultState().withProperty(PROP_STAGE, GelStage.fromMeta(meta & 7));
//    }

//    @Override
//    public int getMetaFromState(IBlockState state) {
//        return state.getValue(PROP_STAGE).getMeta();
//    }

    // Logic

    @Override
//    public void updateTick(World world, BlockPos pos, BlockState state, Random rand)
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        GelStage stage = state.getValue(PROP_STAGE);
        GelStage next = stage.next();
        BlockState nextState = state.setValue(PROP_STAGE, next);
        if (stage.spreading) {
            Deque<BlockPos> openQueue = new ArrayDeque<>();
            Set<BlockPos> seenSet = new HashSet<>();
            List<BlockPos> changeable = new ArrayList<>();
            List<Direction> faces = new ArrayList<>();
            Collections.addAll(faces, Direction.values());
            Collections.shuffle(faces);
            seenSet.add(pos);
            for (Direction face : faces) {
                openQueue.add(pos.relative(face));
            }
            Collections.shuffle(faces);
            int tries = 0;

            while (openQueue.size() > 0 && changeable.size() < 3 && tries < 10_000) {
                BlockPos test = openQueue.removeFirst();

                boolean water = isWater(world, test);
                boolean spreadable = water || canSpread(world, test);

                // Calen: 1.18.2 source level = 8, instead of 0 in 1.12.2
//                if (water && world.getBlockState(test).getValue(BlockLiquid.LEVEL) == 0)
                if (water && world.getFluidState(test).isSource()) {
                    changeable.add(test);
                }
                if (spreadable) {
                    Collections.shuffle(faces);
                    for (Direction face : faces) {
                        BlockPos n = test.relative(face);
                        if (seenSet.add(n)) {
                            openQueue.add(n);
                        }
                    }
                }
                tries++;
            }
            final int time = next.spreading ? 200 : 400;
            if (changeable.size() == 3 || world.random.nextDouble() < 0.5) {
                for (BlockPos p : changeable) {
                    world.setBlock(p, nextState, BlockConstants.UPDATE_ALL);
                    world.getBlockTicks().scheduleTick(p, this, rand.nextInt(150) + time);
                }
                world.setBlock(pos, nextState, BlockConstants.UPDATE_ALL);
                SoundUtil.playBlockPlace(world, pos);
            }
            world.getBlockTicks().scheduleTick(pos, this, rand.nextInt(150) + time);
        } else if (stage != next) {
            if (notTouchingWater(world, pos)) {
                world.setBlock(pos, nextState, BlockConstants.UPDATE_ALL);
                world.getBlockTicks().scheduleTick(pos, this, rand.nextInt(150) + 400);
            } else {
                world.getBlockTicks().scheduleTick(pos, this, rand.nextInt(150) + 600);
            }
        }
    }

    private static boolean notTouchingWater(World world, BlockPos pos) {
        for (Direction face : Direction.values()) {
            if (isWater(world, pos.relative(face))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWater(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() == Blocks.WATER;
    }

    private boolean canSpread(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == this) {
            return true;
        }
        return false;
    }

    // Misc

    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
        GelStage stage = state.getValue(PROP_STAGE);
        return stage.soundType;
    }

    @Override
//    public float getBlockHardness(BlockState state, World world, BlockPos pos)
    public float getDestroyProgress(BlockState state, PlayerEntity player, IBlockReader world, BlockPos pos) {
        GelStage stage = state.getValue(PROP_STAGE);
        float f = stage.hardness;
        // Calen: below is from super
        if (f == -1.0F) {
            return 0.0F;
        } else {
            int i = net.minecraftforge.common.ForgeHooks.canHarvestBlock(state, player, world, pos) ? 30 : 100;
            return player.getDigSpeed(state, pos) / f / (float) i;
        }
    }

    // 1.18.2: use datagen
//    @Override
//    public int quantityDropped(IBlockState state, int fortune, Random random) {
//        GelStage stage = state.getValue(PROP_STAGE);
//        if (stage.spreading) {
//            return random.nextInt(2) + 1;
//        } else {
//            return 1;
//        }
//    }
}
