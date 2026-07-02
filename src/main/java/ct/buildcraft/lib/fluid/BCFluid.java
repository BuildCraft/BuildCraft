/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package ct.buildcraft.lib.fluid;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

import ct.buildcraft.api.core.BCLog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class BCFluid extends ForgeFlowingFluid {
	private int colour = 0xFFFFFFFF, light = 0xFF_FF_FF_FF, dark = 0xFF_FF_FF_FF;
	private boolean isFlammable = false;
	private int lightOpacity = 0;
	private MaterialColor mapColour = MaterialColor.COLOR_BLACK;
	private int heatLevel;// int heat
	private boolean heatable;
	private String blockName;

	private static MethodHandle canPassThroughWall;

	static {
		try {
			MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(FlowingFluid.class, MethodHandles.lookup());
			MethodType methodType = MethodType.methodType(boolean.class, Direction.class, BlockGetter.class,
					BlockPos.class, BlockState.class, BlockPos.class, BlockState.class);
			try {
				canPassThroughWall = lookup.findSpecial(BCFluid.class, "canPassThroughWall", methodType, FlowingFluid.class);
			} catch (NoSuchMethodException | IllegalAccessException e) {
				canPassThroughWall = lookup.findSpecial(BCFluid.class, "m_76061_", methodType, FlowingFluid.class);
			}

		} catch (NoSuchMethodException | IllegalAccessException e) {
			BCLog.logger.fatal("Cannot find method[canPassThroughWall] in class[%s]", FlowingFluid.class.getName());
			throw new RuntimeException(e);
		}
	}

	protected BCFluid(Properties properties) {
		super(properties);

	}

	@Override
	protected void spread(LevelAccessor getter, BlockPos orginPos, FluidState fluidState) {
		boolean lighterThanAir = this.getFluidType().isLighterThanAir();
		if (!lighterThanAir) {
			super.spread(getter, orginPos, fluidState);
			return;
		}

		if (!fluidState.isEmpty()) {

			BlockState blockstate = getter.getBlockState(orginPos);
			BlockPos blockpos = lighterThanAir ? orginPos.above() : orginPos.below();
			BlockState blockstate1 = getter.getBlockState(blockpos);
			FluidState fluidstate = this.getNewLiquid(getter, blockpos, blockstate1);
			if (this.canSpreadTo(getter, orginPos, blockstate, Direction.UP, blockpos, blockstate1, getter.getFluidState(blockpos), fluidstate.getType())) {
				this.spreadTo(getter, blockpos, blockstate1, Direction.UP, fluidstate);
				//this.sourceNeighborCount(p_76011_, p_76012_) >= 3)
				int i = 0;
				{

					for (Direction direction1 : Direction.Plane.HORIZONTAL) {
						BlockPos blockpos1 = orginPos.relative(direction1);
						FluidState fluidstate1 = getter.getFluidState(blockpos1);
						if (fluidstate1.getType().isSame(this) && fluidstate1.isSource()) {
							++i;
						}
					}
				}
				if (i >= 3) {
					// this.spreadToSides(p_76011_, p_76012_, p_76013_, blockstate);
					{
						int i0 = fluidState.getAmount() - this.getDropOff(getter);
						if (blockstate.getValue(FALLING)) {
							i0 = 7;
						}

						if (i0 > 0) {
							Map<Direction, FluidState> map = this.getSpread(getter, orginPos, blockstate);

							for (Map.Entry<Direction, FluidState> entry : map.entrySet()) {
								Direction direction2 = entry.getKey();
								FluidState fluidstate2 = entry.getValue();
								BlockPos blockpos2 = orginPos.relative(direction2);
								BlockState blockstate2 = getter.getBlockState(blockpos2);
								if (this.canSpreadTo(getter, orginPos, blockstate, direction2, blockpos2, blockstate2,
										getter.getFluidState(blockpos2), fluidstate2.getType())) {
									this.spreadTo(getter, blockpos2, blockstate2, direction2, fluidstate2);
								}
							}

						}
					}
				}
			} else if (fluidState.isSource()
					|| !(canPassThroughWall0(Direction.UP, getter, orginPos, blockstate, blockpos, blockstate1)
							&& (blockstate1.getFluidState().getType().isSame(this)
									|| this.canHoldFluid(getter, blockpos, blockstate1, fluidstate.getType())))) {
				//this.spreadToSides(p_76011_, p_76012_, p_76013_, blockstate);
				{
					int i1 = fluidState.getAmount() - this.getDropOff(getter);
					if (fluidState.getValue(FALLING)) {
						i1 = 7;
					}

					if (i1 > 0) {
						Map<Direction, FluidState> map = this.getSpread(getter, orginPos, blockstate);

						for (Map.Entry<Direction, FluidState> entry : map.entrySet()) {
							Direction direction2 = entry.getKey();
							FluidState fluidstate2 = entry.getValue();
							BlockPos blockpos2 = orginPos.relative(direction2);
							BlockState blockstate2 = getter.getBlockState(blockpos2);
							if (this.canSpreadTo(getter, orginPos, blockstate, direction2, blockpos2, blockstate2,
									getter.getFluidState(blockpos2), fluidstate2.getType())) {
								this.spreadTo(getter, blockpos2, blockstate2, direction2, fluidstate2);
							}
						}

					}
				}
			}

		}
	}

	private boolean canHoldFluid(BlockGetter p_75973_, BlockPos p_75974_, BlockState p_75975_, Fluid p_75976_) {
		Block block = p_75975_.getBlock();
		if (block instanceof LiquidBlockContainer) {
			return ((LiquidBlockContainer) block).canPlaceLiquid(p_75973_, p_75974_, p_75975_, p_75976_);
		} else if (!(block instanceof DoorBlock) && !p_75975_.is(BlockTags.SIGNS) && !p_75975_.is(Blocks.LADDER)
				&& !p_75975_.is(Blocks.SUGAR_CANE) && !p_75975_.is(Blocks.BUBBLE_COLUMN)) {
			Material material = p_75975_.getMaterial();
			if (material != Material.PORTAL && material != Material.STRUCTURAL_AIR && material != Material.WATER_PLANT
					&& material != Material.REPLACEABLE_WATER_PLANT) {
				return !material.blocksMotion();
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean canPassThroughWall0(Direction direction, BlockGetter getter, BlockPos orginPos,
			BlockState orginState, BlockPos targePos, BlockState targeBlock) {
		try {
			return (boolean) canPassThroughWall.invoke(this, direction, getter, orginPos, orginState, targePos,
					targeBlock);
		} catch (Throwable e) {
			BCLog.logger.error("BCFluid:invoke error");
			return false;
		}
	}

	protected FluidState getNewLiquid(LevelReader p_76036_, BlockPos p_76037_, BlockState p_76038_) {
		if (!this.getFluidType().isLighterThanAir())
			return super.getNewLiquid(p_76036_, p_76037_, p_76038_);
		int i = 0;
		int j = 0;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockpos = p_76037_.relative(direction);
			BlockState blockstate = p_76036_.getBlockState(blockpos);
			FluidState fluidstate = blockstate.getFluidState();
			if (fluidstate.getType().isSame(this) && canPassThroughWall0(direction, (BlockGetter) p_76036_, p_76037_,
					p_76038_, blockpos, blockstate)) {
				if (fluidstate.isSource() && net.minecraftforge.event.ForgeEventFactory.canCreateFluidSource(p_76036_,
						blockpos, blockstate, fluidstate.canConvertToSource(p_76036_, blockpos))) {
					++j;
				}

				i = Math.max(i, fluidstate.getAmount());
			}
		}

		if (j >= 2) {
			BlockState blockstate1 = p_76036_.getBlockState(p_76037_.above());
			FluidState fluidstate1 = blockstate1.getFluidState();
			if (blockstate1.getMaterial().isSolid() || fluidstate1.getType().isSame(this) && fluidstate1.isSource()) {
				return this.getSource(false);
			}
		}

		BlockPos blockpos1 = p_76037_.below();
		BlockState blockstate2 = p_76036_.getBlockState(blockpos1);
		FluidState fluidstate2 = blockstate2.getFluidState();
		if (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && canPassThroughWall0(Direction.DOWN,
				(BlockGetter) p_76036_, p_76037_, p_76038_, blockpos1, blockstate2)) {
			return this.getFlowing(8, true);
		} else {
			int k = i - this.getDropOff(p_76036_);
			return k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
		}
	}

	@Override
	protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluidIn,
			Direction direction) {
		return fluidIn.getFluidType().getDensity() > this.getFluidType().getDensity();// 1.19.2:this should be forge
																						// work
	}

	@Override
	protected boolean canSpreadTo(BlockGetter p_75978_, BlockPos p_75979_, BlockState p_75980_, Direction p_75981_,
			BlockPos p_75982_, BlockState p_75983_, FluidState fluidState, Fluid p_75985_) {
		return super.canSpreadTo(p_75978_, p_75979_, p_75980_, p_75981_, p_75982_, p_75983_, fluidState, p_75985_)
				&& (fluidState.isEmpty() || fluidState.getFluidType().getDensity() < this.getFluidType().getDensity());
	}

	public Component getBareLocalizedName(FluidStack stack) {
		return super.getFluidType().getDescription(stack);
	}

	public void setMapColour(MaterialColor mapColour) {
		this.mapColour = mapColour;
	}

	public final MaterialColor getMapColour() {
		return this.mapColour;
	}

	public void setFlammable(boolean isFlammable) {
		this.isFlammable = isFlammable;
	}

	public final boolean isFlammable() {
		return isFlammable;
	}

	public void setLightOpacity(int lightOpacity) {
		this.lightOpacity = lightOpacity;
	}

	public final int getLightOpacity() {
		return lightOpacity;
	}

	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}

	public String getBlockName() {
		return blockName;
	}

	public int getColor() {
		return colour;
	}

	public int getLightColour() {
		return light;
	}

	public int getDarkColour() {
		return dark;
	}

	public BCFluid setColour(int colour) {
		this.colour = colour;
		return this;
	}

	public BCFluid setColour(int light, int dark) {
		this.light = light;
		this.dark = dark;
		this.colour = 0xFF_FF_FF_FF;
		return this;
	}

	public BCFluid setHeat(int heat) {
		this.heatLevel = heat;
		return this;
	}

	public int getHeatValue() {
		return heatLevel;
	}

	public BCFluid setHeatable(boolean value) {
		heatable = value;
		return this;
	}

	public boolean isHeatable() {
		return heatable;
	}

	public static class Flowing extends BCFluid {
		public Flowing(Properties properties) {
			super(properties);
			registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
		}

		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		public boolean isSource(FluidState state) {
			return false;
		}
	}

	public static class Source extends BCFluid {
		public Source(Properties properties) {
			super(properties);
		}

		public int getAmount(FluidState state) {
			return 8;
		}

		public boolean isSource(FluidState state) {
			return true;
		}
	}

}
