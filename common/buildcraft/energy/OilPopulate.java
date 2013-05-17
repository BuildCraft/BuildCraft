/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.terraingen.TerrainGen;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import com.google.common.collect.MapMaker;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.EnumHelper;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import net.minecraftforge.event.world.ChunkDataEvent;

public class OilPopulate {

	public static final OilPopulate INSTANCE = new OilPopulate();
	public static final EventType EVENT_TYPE = EnumHelper.addEnum(EventType.class, "BUILDCRAFT_OIL", new Class[0], new Object[0]);
	private static final String CHUNK_TAG = "buildcraft";
	private static final String GROUND_TAG = "oilGenGroundLevel";
	private static final byte GEN_AREA = 2;
	private static final byte LARGE_WELL_HEIGHT = 16;
	private static final byte MEDIUM_WELL_HEIGHT = 4;
	public final Set<Integer> surfaceDepositBiomes = new HashSet<Integer>();
	public final Set<Integer> excludedBiomes = new HashSet<Integer>();
	private final Map<Chunk, Integer> chunkData = new MapMaker().weakKeys().makeMap();

	private enum GenType {

		LARGE, MEDIUM, LAKE, NONE
	};

	private OilPopulate() {
		BuildCraftCore.debugMode = true;
		surfaceDepositBiomes.add(BiomeGenBase.desert.biomeID);
		surfaceDepositBiomes.add(BiomeGenBase.taiga.biomeID);

		excludedBiomes.add(BiomeGenBase.sky.biomeID);
		excludedBiomes.add(BiomeGenBase.hell.biomeID);
	}

	@ForgeSubscribe
	public void populate(PopulateChunkEvent.Populate event) {
		if (event.type != EventType.LAKE) {
			return;
		}
		boolean doGen = TerrainGen.populate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkX, event.hasVillageGenerated, EVENT_TYPE);

		if (!doGen) {
			return;
		}

		generateOil(event.world, event.rand, event.chunkX, event.chunkZ);
	}

	public void generateOil(World world, Random rand, int chunkX, int chunkZ) {
		Chunk targetChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);

//		if (chunkX == 16 && chunkZ == 40) {
//			System.out.println("target");
//		}
		for (int i = -GEN_AREA; i <= GEN_AREA; i++) {
			for (int k = -GEN_AREA; k <= GEN_AREA; k++) {
				int cx = chunkX + i;
				int cz = chunkZ + k;
				if (cx == chunkX && cz == chunkZ) {
					continue;
				}
				if (world.getChunkProvider().chunkExists(cx, cz)) {
					Chunk currentChunk = world.getChunkFromChunkCoords(cx, cz);
					Random r = getRandom(world, cx, cz);
					doPopulate(world, r, targetChunk, currentChunk);
				}
			}
		}

		Random r = getRandom(world, chunkX, chunkZ);
		doPopulate(world, r, null, targetChunk);
	}

	private Random getRandom(World world, int chunkX, int chunkZ) {
		return new Random(world.getSeed() + chunkX + chunkZ * 90373);
	}

	private void doPopulate(World world, Random rand, Chunk targetChunk, Chunk currentChunk) {

//		if (currentChunk.xPosition == 17 && currentChunk.zPosition == 41) {
//			System.out.println("source");
//		}

		// shift to world coordinates
		int x = currentChunk.xPosition * 16 + rand.nextInt(16);
		int z = currentChunk.zPosition * 16 + rand.nextInt(16);

		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

		// Do not generate oil in the End or Nether
		if (excludedBiomes.contains(biome.biomeID)) {
			return;
		}

		boolean oilBiome = surfaceDepositBiomes.contains(biome.biomeID);

		double bonus = oilBiome ? 1.5 : 1.0;
		if (BuildCraftCore.debugMode) {
			bonus *= 20;
		}
		GenType type = GenType.NONE;
		if (rand.nextDouble() <= 0.0005 * bonus) {// 0.05%
			type = GenType.LARGE;
		} else if (rand.nextDouble() <= 0.0015 * bonus) {// 0.15%
			type = GenType.MEDIUM;
		} else if (oilBiome && rand.nextDouble() <= 0.02) {// 2%
			type = GenType.LAKE;
		}
//		if (currentChunk.xPosition == 17 && currentChunk.zPosition == 41 && type != GenType.NONE) {
//			System.out.println("well!");
//		}
		if (type == GenType.NONE) {
			return;
		}

		// Find ground level
		int groundLevel = getGroundLevel(currentChunk);
		if (groundLevel <= 0) {
			groundLevel = getTopBlock(world, currentChunk, x, z, type);
			setGroundLevel(currentChunk, groundLevel);
		}

		// Generate a Well
		if (type == GenType.LARGE || type == GenType.MEDIUM) {
			int wellX = x;
			int wellZ = z;

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

						if (distance <= radiusSq && canReplaceInChunk(targetChunk, poolX + wellX, poolZ + wellZ)) {
							world.setBlock(poolX + wellX, poolY + wellY, poolZ + wellZ, BuildCraftEnergy.oilStill.blockID);
						}
					}
				}
			}

			// Generate Lake around Spout
			int lakeRadius;
			if (type == GenType.LARGE) {
				lakeRadius = 20 + rand.nextInt(20);
				if (BuildCraftCore.debugMode) {
					lakeRadius += 40;
				}
			} else {
				lakeRadius = 5 + rand.nextInt(5);
			}
			generateSurfaceDeposit(world, rand, targetChunk, wellX, groundLevel, wellZ, lakeRadius);

			// Generate Spout
			int baseY;
			if (type == GenType.LARGE && BuildCraftEnergy.spawnOilSprings && (BuildCraftCore.debugMode || rand.nextDouble() <= 0.25)) {
				baseY = 0;
			} else {
				baseY = wellY;
			}

			if (canReplaceInChunk(targetChunk, wellX, wellZ)) {
				if (world.getBlockId(wellX, baseY, wellZ) == Block.bedrock.blockID) {
					if (BuildCraftEnergy.spawnOilSprings) {
						world.setBlock(wellX, baseY, wellZ, BuildCraftCore.springBlock.blockID, 1, 3);
					}
				}
				for (int y = baseY + 1; y <= maxHeight; ++y) {
					world.setBlock(wellX, y, wellZ, BuildCraftEnergy.oilStill.blockID);
				}
			}

		} else if (type == GenType.LAKE) {
			// Generate a surface oil lake
			int lakeX = x;
			int lakeZ = z;
			int lakeY = groundLevel;

			int blockId = world.getBlockId(lakeX, lakeY, lakeZ);
			if (blockId == biome.topBlock) {
				generateSurfaceDeposit(world, rand, targetChunk, lakeX, lakeY, lakeZ, 2 + rand.nextInt(5));
			}
		}
	}

	public void generateSurfaceDeposit(World world, Random rand, int x, int y, int z, int radius) {
		generateSurfaceDeposit(world, rand, null, x, y, z, radius);
	}

	private void generateSurfaceDeposit(World world, Random rand, Chunk targetChunk, int x, int y, int z, int radius) {
		int depth = rand.nextDouble() < 0.5 ? 1 : 2;

		// Center
		setOilForLake(world, targetChunk, x, y, z, depth);

		// Generate tendrils, from the center outward
		for (int w = 1; w <= radius; ++w) {
			float proba = (float) (radius - w + 4) / (float) (radius + 4);

			for (int d = -w; d <= w; ++d) {
				setOilWithProba(world, targetChunk, rand, proba, x + d, y, z + w, depth);
				setOilWithProba(world, targetChunk, rand, proba, x + d, y, z - w, depth);
				setOilWithProba(world, targetChunk, rand, proba, x + w, y, z + d, depth);
				setOilWithProba(world, targetChunk, rand, proba, x - w, y, z + d, depth);

			}
		}

		// Fill in holes
		for (int dx = x - radius; dx <= x + radius; ++dx) {
			for (int dz = z - radius; dz <= z + radius; ++dz) {
				if (!canReplaceInChunk(targetChunk, dx, dz)) {
					continue;
				}
				if (isOil(world, dx, y - 1, dz)) {
					continue;
				}
				if (isOilSurrounded(world, dx, y - 1, dz)) {
					setOilForLake(world, targetChunk, dx, y, dz, depth);
				}
			}
		}
	}

	private boolean isOilOrWater(World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z)) {
			return false;
		}
		int blockId = world.getBlockId(x, y, z);
		return blockId == Block.waterMoving.blockID || blockId == Block.waterStill.blockID || blockId == BuildCraftEnergy.oilStill.blockID || blockId == BuildCraftEnergy.oilMoving.blockID;
	}

	private boolean isOil(World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z)) {
			return false;
		}
		int blockId = world.getBlockId(x, y, z);
		return (blockId == BuildCraftEnergy.oilStill.blockID || blockId == BuildCraftEnergy.oilMoving.blockID);
	}

	private boolean isOilAdjacent(World world, int x, int y, int z) {
		return isOil(world, x + 1, y, z)
				|| isOil(world, x - 1, y, z)
				|| isOil(world, x, y, z + 1)
				|| isOil(world, x, y, z - 1);
	}

	private boolean isOilSurrounded(World world, int x, int y, int z) {
		return isOil(world, x + 1, y, z)
				&& isOil(world, x - 1, y, z)
				&& isOil(world, x, y, z + 1)
				&& isOil(world, x, y, z - 1);
	}

	private void setOilWithProba(World world, Chunk targetChunk, Random rand, float proba, int x, int y, int z, int depth) {
//		if (x / 16 == 16 && z / 16 == 40) {
//			System.out.println("spawn tendril");
//		}
		if (!world.blockExists(x, y, z)) {
			return;
		}
		if (rand.nextFloat() <= proba && world.getBlockId(x, y - 2, z) != 0) {
			if (isOilAdjacent(world, x, y - 1, z)) {
				setOilForLake(world, targetChunk, x, y, z, depth);
			}
		}
	}

	private void setOilForLake(World world, Chunk targetChunk, int x, int y, int z, int depth) {
		if (!canReplaceInChunk(targetChunk, x, z)) {
			return;
		}
		if (!world.blockExists(x, y, z)) {
			return;
		}
		if (world.isAirBlock(x, y + 1, z) || !world.isBlockOpaqueCube(x, y + 1, z) || Block.blocksList[world.getBlockId(x, y + 1, z)] instanceof BlockFlower) {
			world.setBlockToAir(x, y + 1, z);
			if (isOilOrWater(world, x, y, z)) {
				world.setBlock(x, y, z, BuildCraftEnergy.oilStill.blockID);
			} else {
				world.setBlock(x, y, z, 0, 0, 2);
			}

			for (int d = depth; d > 0; d--) {
				if (isOilOrWater(world, x, y - d - 1, z) || world.isBlockSolidOnSide(x, y - d - 1, z, ForgeDirection.UP)) {
					world.setBlock(x, y - d, z, BuildCraftEnergy.oilStill.blockID);
				}
			}
		}
	}

	@ForgeSubscribe
	public void saveChunk(ChunkDataEvent.Save event) {
		Integer groundLevel = chunkData.remove(event.getChunk());
		if (groundLevel != null) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger(GROUND_TAG, groundLevel);
			event.getData().setTag(CHUNK_TAG, nbt);
		}
	}

	@ForgeSubscribe
	public void loadChunk(ChunkDataEvent.Load event) {
		if (!chunkData.containsKey(event.getChunk())) {
			Integer groundLevel = event.getData().getCompoundTag(CHUNK_TAG).getInteger(GROUND_TAG);
			if (groundLevel > 0) {
				chunkData.put(event.getChunk(), groundLevel);
			}
		}
	}

	private boolean canReplaceInChunk(Chunk targetChunk, int x, int z) {
//		if (x / 16 == 16 && z / 16 == 40) {
//			System.out.println("fill target");
//		}
		if (targetChunk == null) {
			return true;
		}
		return targetChunk.xPosition == x / 16 && targetChunk.zPosition == z / 16;
	}

	private void setGroundLevel(Chunk chunk, int height) {
		chunkData.put(chunk, height);
	}

	private int getGroundLevel(Chunk chunk) {
		Integer groundLevel = chunkData.get(chunk);
		return groundLevel != null ? groundLevel : -1;
	}

	private int getTopBlock(World world, Chunk chunk, int x, int z, GenType type) {
		int y = chunk.getTopFilledSegment() + 15;

		int trimmedX = x & 15;
		int trimmedZ = z & 15;

		for (; y > 0; --y) {
			int blockId = chunk.getBlockID(trimmedX, y, trimmedZ);
			Block block = Block.blocksList[blockId];
			if (blockId == 0 || block.blockMaterial == Material.leaves) {
				continue;
			}
			if (blockId == BuildCraftEnergy.oilStill.blockID && y > 70) {
				System.out.println("Fail!");
				if (type != GenType.LAKE) {
					int wellHeight = type == GenType.LARGE ? LARGE_WELL_HEIGHT : MEDIUM_WELL_HEIGHT;
					int groundLevel = y - wellHeight;
					blockId = chunk.getBlockID(trimmedX, groundLevel, trimmedZ);
					if (blockId == BuildCraftEnergy.oilStill.blockID) {
						return groundLevel;
					}
				}
				return y;
			}
			if (block instanceof BlockFlower) {
				continue;
			}
			if (Block.blocksList[blockId].isWood(world, x, y, z)) {
				continue;
			}
			if (Block.blocksList[blockId].isBlockFoliage(world, x, y, z)) {
				continue;
			}
			return y;
		}

		return -1;
	}
}
