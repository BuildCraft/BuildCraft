/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.worldgen;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.pattern.BlockHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;

public final class OilPopulate {

	public static final OilPopulate INSTANCE = new OilPopulate();
	public static final EventType EVENT_TYPE = EnumHelper.addEnum(EventType.class, "BUILDCRAFT_OIL", new Class[0], new Object[0]);
	private static final byte LARGE_WELL_HEIGHT = 16;
	private static final byte MEDIUM_WELL_HEIGHT = 6;
	public final Set<Integer> excessiveBiomes = new HashSet<Integer>();
	public final Set<Integer> surfaceDepositBiomes = new HashSet<Integer>();
	public final Set<Integer> excludedBiomes = new HashSet<Integer>();

	private enum GenType {
		LARGE, MEDIUM, LAKE, NONE
	}

	private OilPopulate() {
//		BuildCraftCore.debugWorldgen = true;
	}

	@SubscribeEvent
	public void populate(PopulateChunkEvent.Pre event) {
		boolean doGen = TerrainGen.populate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ, event.hasVillageGenerated, EVENT_TYPE);

		if (!doGen) {
			event.setResult(Result.ALLOW);
			return;
		}

		generateOil(event.world, event.rand, event.chunkX, event.chunkZ);
	}

	public void generateOil(World world, Random rand, int chunkX, int chunkZ) {

		// shift to world coordinates
		BlockPos p = new BlockPos(chunkX * 16 + 8 + rand.nextInt(16), 0, chunkZ * 16 + 8 + rand.nextInt(16));

		BiomeGenBase biome = world.getBiomeGenForCoords(p);

		// Do not generate oil in the End or Nether
		if (excludedBiomes.contains(biome.biomeID)) {
			return;
		}

		boolean oilBiome = surfaceDepositBiomes.contains(biome.biomeID);

		double bonus = oilBiome ? 3.0 : 1.0;
		bonus *= BuildCraftEnergy.oilWellScalar;
		if (excessiveBiomes.contains(biome.biomeID)) {
			bonus *= 30.0;
		} else if (BuildCraftCore.debugWorldgen) {
			bonus *= 20.0;
		}
		GenType type = GenType.NONE;
		if (rand.nextDouble() <= 0.0004 * bonus) {
			// 0.04%
			type = GenType.LARGE;
		} else if (rand.nextDouble() <= 0.001 * bonus) {
			// 0.1%
			type = GenType.MEDIUM;
		} else if (oilBiome && rand.nextDouble() <= 0.02 * bonus) {
			// 2%
			type = GenType.LAKE;
		}

		if (type == GenType.NONE) {
			return;
		}


		// Find ground level
		int groundLevel = getTopBlock(world, p);
		if (groundLevel < 5) {
			return;
		}

		double deviation = surfaceDeviation(world, p.offsetUp(groundLevel), 8);
		if (deviation > 0.45) {
			return;
		}

		// Generate a Well
		if (type == GenType.LARGE || type == GenType.MEDIUM) {
			BlockPos wellPos = new BlockPos(p);

			int wellHeight = MEDIUM_WELL_HEIGHT;
			if (type == GenType.LARGE) {
				wellHeight = LARGE_WELL_HEIGHT;
			}
			int maxHeight = groundLevel + wellHeight;
			if (maxHeight >= world.getActualHeight() - 1) {
				return;
			}

			// Generate a spherical cave deposit
			int wellY = 20 + rand.nextInt(10);

			int radius;
			if (type == GenType.LARGE) {
				radius = 8 + rand.nextInt(9);
			} else {
				radius = 4 + rand.nextInt(4);
			}

			int radiusSq = radius * radius;

			for (int poolX = -radius; poolX <= radius; poolX++) {
				for (int poolY = -radius; poolY <= radius; poolY++) {
					for (int poolZ = -radius; poolZ <= radius; poolZ++) {
						int distance = poolX * poolX + poolY * poolY + poolZ * poolZ;

						if (distance <= radiusSq) {
							world.setBlockState(new BlockPos(poolX + wellPos.getX(), poolY + wellY, poolZ + wellPos.getZ()), BuildCraftEnergy.blockOil.getDefaultState(), distance == radiusSq ? 3 : 2);
						}
					}
				}
			}

			// Generate Lake around Spout
			int lakeRadius;
			if (type == GenType.LARGE) {
				lakeRadius = 25 + rand.nextInt(20);
//				if (BuildCraftCore.debugMode) {
//					lakeRadius += 40;
//				}
			} else {
				lakeRadius = 5 + rand.nextInt(10);
			}
			generateSurfaceDeposit(world, rand, biome, wellPos.offsetUp(groundLevel), lakeRadius);

			boolean makeSpring = type == GenType.LARGE && BuildCraftEnergy.spawnOilSprings && BuildCraftCore.springBlock != null && (BuildCraftCore.debugWorldgen || rand.nextDouble() <= 0.25);

			// Generate Spout
			int baseY;
			if (makeSpring) {
				baseY = 0;
			} else {
				baseY = wellY;
			}

			if (makeSpring && world.getBlockState(wellPos.offsetUp(baseY)).getBlock() == Blocks.bedrock) {
				//FIXME: Apply the good metadata!
				world.setBlockState(wellPos.offsetUp(baseY), BuildCraftCore.springBlock.getDefaultState(), 3);
			}
			for (int y = baseY + 1; y <= maxHeight; ++y) {
				world.setBlockState(wellPos.offsetUp(y), BuildCraftEnergy.blockOil.getDefaultState(), 3);
			}

			if (type == GenType.LARGE) {
				for (int y = wellY; y <= maxHeight - wellHeight / 2; ++y) {
					BlockPos targetPos = new BlockPos(wellPos).offsetUp(y);
					world.setBlockState(targetPos.offsetEast(), BuildCraftEnergy.blockOil.getDefaultState(), 3);
					world.setBlockState(targetPos.offsetWest(), BuildCraftEnergy.blockOil.getDefaultState(), 3);
					world.setBlockState(targetPos.offsetSouth(), BuildCraftEnergy.blockOil.getDefaultState(), 3);
					world.setBlockState(targetPos.offsetNorth(), BuildCraftEnergy.blockOil.getDefaultState(), 3);
				}
			}

		} else if (type == GenType.LAKE) {
			// Generate a surface oil lake
			BlockPos lakePos = new BlockPos(p).offsetUp(groundLevel);
			
			Block block = world.getBlockState(lakePos).getBlock();
			if (block == biome.topBlock) {
				generateSurfaceDeposit(world, rand, biome, lakePos, 5 + rand.nextInt(10));
			}
		}
	}

	public void generateSurfaceDeposit(World world, Random rand, BlockPos pos, int radius) {
		BiomeGenBase biome = world.getBiomeGenForCoords(pos);
		generateSurfaceDeposit(world, rand, biome, pos, radius);
	}

	private void generateSurfaceDeposit(World world, Random rand, BiomeGenBase biome, BlockPos pos, int radius) {
		int depth = rand.nextDouble() < 0.5 ? 1 : 2;

		// Center
		setOilColumnForLake(world, biome, pos, depth, 2);

		// Generate tendrils, from the center outward
		for (int w = 1; w <= radius; ++w) {
			float proba = (float) (radius - w + 4) / (float) (radius + 4);

			setOilWithProba(world, biome, rand, proba, pos.offsetSouth(w), depth);
			setOilWithProba(world, biome, rand, proba, pos.offsetNorth(w), depth);
			setOilWithProba(world, biome, rand, proba, pos.offsetEast(w), depth);
			setOilWithProba(world, biome, rand, proba, pos.offsetWest(w), depth);

			for (int i = 1; i <= w; ++i) {
				setOilWithProba(world, biome, rand, proba, pos.add(i, 0, w), depth);
				setOilWithProba(world, biome, rand, proba, pos.add(i, 0, -w), depth);
				setOilWithProba(world, biome, rand, proba, pos.add(w, 0, i), depth);
				setOilWithProba(world, biome, rand, proba, pos.add(-w, 0, i), depth);

				setOilWithProba(world, biome, rand, proba, pos.add(-i, 0, w), depth);
				setOilWithProba(world, biome, rand, proba, pos.add(-i, 0, -w), depth);
				setOilWithProba(world, biome, rand, proba, pos.add(w, 0, -i), depth);
				setOilWithProba(world, biome, rand, proba, pos.add(-w, 0, -i), depth);
			}
		}

		// Fill in holes
		for (int dx = pos.getX() - radius; dx <= pos.getX() + radius; ++dx) {
			for (int dz = pos.getZ() - radius; dz <= pos.getZ() + radius; ++dz) {
				if (isOil(world, new BlockPos(dx, pos.getY(), dz))) {
					continue;
				}
				if (isOilSurrounded(world, new BlockPos(dx, pos.getY(), dz))) {
					setOilColumnForLake(world, biome, new BlockPos(dx, pos.getY(), dz), depth, 2);
				}
			}
		}
	}

	private boolean isReplaceableFluid(World world, BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		return (block instanceof BlockStaticLiquid || block instanceof BlockFluidBase || block instanceof IFluidBlock) && block.getMaterial() != Material.lava;
	}

	private boolean isOil(World world, BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		return block == BuildCraftEnergy.blockOil;
	}

	private boolean isReplaceableForLake(World world, BiomeGenBase biome, BlockPos pos) {
		if (world.isAirBlock(pos)) {
			return true;
		}
		
		Block block = world.getBlockState(pos).getBlock();

		if (block == biome.fillerBlock || block == biome.topBlock) {
			return true;
		}

		if (!block.getMaterial().blocksMovement()) {
			return true;
		}

		if (block.isReplaceableOreGen(world, pos, BlockHelper.forBlock(Blocks.stone))) {
			return true;
		}

		if (block instanceof BlockFlower) {
			return true;
		}

		if (!block.isOpaqueCube()) {
			return true;
		}

		return false;
	}

	private boolean isOilAdjacent(World world, BlockPos pos) {
		return isOil(world, pos.offsetEast())
				|| isOil(world, pos.offsetWest())
				|| isOil(world, pos.offsetSouth())
				|| isOil(world, pos.offsetNorth());
	}

	private boolean isOilSurrounded(World world, BlockPos pos) {
		return isOil(world, pos.offsetEast())
				&& isOil(world, pos.offsetWest())
				&& isOil(world, pos.offsetSouth())
				&& isOil(world, pos.offsetNorth());
	}

	private void setOilWithProba(World world, BiomeGenBase biome, Random rand, float proba, BlockPos pos, int depth) {
		if (rand.nextFloat() <= proba && !world.isAirBlock(pos.offsetDown(depth - 1))) {
			if (isOilAdjacent(world, pos)) {
				setOilColumnForLake(world, biome, pos, depth, 3);
			}
		}
	}

	private void setOilColumnForLake(World world, BiomeGenBase biome, BlockPos pos, int depth, int update) {
		if (isReplaceableForLake(world, biome, pos.offsetUp())) {
			if (!world.isAirBlock(pos.offsetUp(2))) {
				return;
			}
			if (isReplaceableFluid(world, pos) || world.isSideSolid(pos.offsetDown(), EnumFacing.UP)) {
				world.setBlockState(pos, BuildCraftEnergy.blockOil.getDefaultState(), update);
			} else {
				return;
			}
			if (!world.isAirBlock(pos.offsetUp())) {
				world.setBlockState(pos.offsetUp(), Blocks.air.getDefaultState(), update);
			}

			for (int d = 1; d <= depth - 1; d++) {
				if (isReplaceableFluid(world, pos.offsetDown(d)) || !world.isSideSolid(pos.offsetDown(d - 1), EnumFacing.UP)) {
					return;
				}
				world.setBlockState(pos.offsetDown(d), BuildCraftEnergy.blockOil.getDefaultState(), 2);
			}
		}
	}

	private int getTopBlock(World world, BlockPos pos) {
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		int y = chunk.getTopFilledSegment() + 15;

		int trimmedX = pos.getX() & 15;
		int trimmedZ = pos.getZ() & 15;

		for (; y > 0; --y) {
			Block block = chunk.getBlock(trimmedX, y, trimmedZ);

			if (block.isAir(world, pos)) {
				continue;
			}

			if (block instanceof BlockStaticLiquid) {
				return y;
			}

			if (block instanceof BlockFluidBase) {
				return y;
			}

			if (block instanceof IFluidBlock) {
				return y;
			}

			if (!block.getMaterial().blocksMovement()) {
				continue;
			}

			if (block instanceof BlockFlower) {
				continue;
			}

			return y - 1;
		}

		return -1;
	}

	private double surfaceDeviation(World world, BlockPos pos, int radius) {
		int diameter = radius * 2;
		double centralTendancy = pos.getY();
		double deviation = 0;
		for (int i = 0; i < diameter; i++) {
			for (int k = 0; k < diameter; k++) {
				deviation += getTopBlock(world, pos.add(-radius + i, 0, -radius + k)) - centralTendancy;
			}
		}
		return Math.abs(deviation / centralTendancy);
	}
}
