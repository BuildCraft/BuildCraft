/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.factory.BCFactoryItems;
import ct.buildcraft.lib.block.BlockBCBase_Neptune;
import ct.buildcraft.lib.misc.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.util.ForgeSoundType;

public class BlockWaterGel extends BlockBCBase_Neptune {
    public enum GelStage implements StringRepresentable {
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
            this.soundType = new ForgeSoundType(//
                SoundType.SLIME_BLOCK.volume, //
                pitch, //
                () -> SoundEvents.SLIME_BLOCK_BREAK, //
                () -> SoundEvents.SLIME_BLOCK_STEP, //
                () -> SoundEvents.SLIME_BLOCK_PLACE, //
                () -> SoundEvents.SLIME_BLOCK_HIT, //
                () -> SoundEvents.SLIME_BLOCK_FALL//
            );
            this.spreading = spreading;
            this.hardness = hardness;
        }

		@Override
		public String getSerializedName() {
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

    public static final EnumProperty<GelStage> PROP_STAGE = EnumProperty.create("stage", GelStage.class);

    public BlockWaterGel() {
        super(BlockBehaviour.Properties.of(Material.CLAY)
        		.sound(SoundType.SLIME_BLOCK).destroyTime(0.2f).explosionResistance(10f));
        this.registerDefaultState(this.stateDefinition.any().setValue(PROP_STAGE, GelStage.SPREAD_0));
    }

    // BlockState

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> bs) {
		bs.add(PROP_STAGE);
		super.createBlockStateDefinition(bs);
	}

    // Logic
    
    @Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
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
                openQueue.add(pos.offset(face.getNormal()));
            }
            Collections.shuffle(faces);
            int tries = 0;

            while (openQueue.size() > 0 && changeable.size() < 3 && tries < 10_000) {
                BlockPos test = openQueue.removeFirst();

                boolean water = isWater(world, test);
                boolean spreadable = water || canSpread(world, test);
                if (water && world.getBlockState(test).getValue(LiquidBlock.LEVEL) == 0) {
                    changeable.add(test);
                }
                if (spreadable) {
                    Collections.shuffle(faces);
                    for (Direction face : faces) {
                        BlockPos n = test.offset(face.getNormal());
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
                    world.setBlockAndUpdate(p, nextState);
                    world.scheduleTick(p, this, rand.nextInt(150) + time);
                }
                world.setBlockAndUpdate(pos, nextState);
                SoundUtil.playBlockPlace(world, pos);
            }
            world.scheduleTick(pos, this, rand.nextInt(150) + time);
        } else if (stage != next) {
            if (notTouchingWater(world, pos)) {
                world.setBlockAndUpdate(pos, nextState);
                world.scheduleTick(pos, this, rand.nextInt(150) + 400);
            } else {
                world.scheduleTick(pos, this, rand.nextInt(150) + 600);
            }
        }
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
        GelStage stage = state.getValue(PROP_STAGE);
        GelStage next = stage.next();
		return stage != next || stage.spreading;
	}

	private static boolean notTouchingWater(Level world, BlockPos pos) {
        for (Direction face : Direction.values()) {
            if (isWater(world, pos.offset(face.getNormal()))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWater(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() == Blocks.WATER;
    }

    private boolean canSpread(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == this) {
            return true;
        }
        return false;
    }

    // Misc

    
    @Override
	public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos,
			@Nullable Entity entity) {
        return state.getValue(PROP_STAGE).soundType;
	}
    
	@Override
	public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return state.getValue(PROP_STAGE).hardness;
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		return ImmutableList.of(new ItemStack(BCFactoryItems.GEL.get(), 
				state.getValue(PROP_STAGE).spreading ? builder.create(LootContextParamSets.BLOCK).getRandom().nextInt(2) + 1 : 1));
	}

}
