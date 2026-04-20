/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.block;

import java.util.function.BiFunction;
import ct.buildcraft.api.enums.EnumSpring;
import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.core.BCCoreItems;
import ct.buildcraft.lib.block.BlockBCBase_Neptune;

import ct.buildcraft.lib.misc.data.XorShift128Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;

public class BlockSpring extends BlockBCBase_Neptune implements EntityBlock {
	public static final EnumProperty<EnumSpring> SPRING_TYPE = BuildCraftProperties.SPRING_TYPE;

	public static final XorShift128Random rand = new XorShift128Random();

	public static boolean springCategoryCounter = false;

	public BlockSpring() {
		super(BlockBehaviour.Properties.of(Material.STONE).strength(-1.0F, 3600000.0F).noLootTable()
				.sound(SoundType.STONE).randomTicks());
		this.registerDefaultState(this.stateDefinition.any().setValue(SPRING_TYPE, EnumSpring.WATER));
	}

	// BlockState

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> bs) {
		bs.add(SPRING_TYPE);
		super.createBlockStateDefinition(bs);
	}

	// Other

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
			Player player) {
		return new ItemStack(BCCoreItems.SPRING_ITEM_MAP.get(state.getValue(SPRING_TYPE)));
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		generateSpringBlock(level, pos, state);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		BiFunction<BlockPos, BlockState, BlockEntity> constructor = state.getValue(SPRING_TYPE).tileConstructor;
		if (constructor != null) {
			return constructor.apply(pos, state);
		}
		return null;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_,
			BlockEntityType<T> p_153214_) {
		// TODO Auto-generated method stub
		return EntityBlock.super.getTicker(p_153212_, p_153213_, p_153214_);
	}

	// @Override
	// public void onNeighborBlockChange(Level Level, int x, int y, int z, int
	// blockid) {
	// assertSpring(Level, x, y, z);
	// }

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity p_49850_, ItemStack p_49851_) {
		super.setPlacedBy(level, pos, state, p_49850_, p_49851_);
		level.scheduleTick(pos, this, state.getValue(SPRING_TYPE).tickRate);

	}

	private void generateSpringBlock(Level Level, BlockPos pos, BlockState state) {
		EnumSpring spring = state.getValue(SPRING_TYPE);
		Level.scheduleTick(pos, this, spring.tickRate);
		if (!spring.canGen || spring.liquidBlock == null) {
			return;
		}
		if (!Level.getBlockState(pos.above()).isAir()) {
			return;
		}
		if (spring.chance != -1 && rand.nextInt(spring.chance) != 0) {
			return;
		}
		Level.setBlockAndUpdate(pos.above(), spring.liquidBlock);
	}

	// Prevents updates on chunk generation
	// @Override
	// public boolean func_149698_L() {
	// return false;
	// }
}
