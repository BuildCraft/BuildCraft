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
import java.util.HashMap;
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
	public final Set<Integer> surfaceDepositBiomes = new HashSet<Integer>();
	public final Set<Integer> excludedBiomes = new HashSet<Integer>();
	private static final String CHUNK_TAG = "buildcraft";
	private static final String GROUND_TAG = "oilGenGroundLevel";
	private static final byte GEN_AREA = 2;
	private static final Map<Chunk, NBTTagCompound> chunkData = new HashMap<Chunk, NBTTagCompound>();
	private static EventType eventType = EnumHelper.addEnum(EventType.class, "BUILDCRAFT_OIL");

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
		boolean doGen = TerrainGen.populate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkX, event.hasVillageGenerated, eventType);

		if (!doGen) {
			return;
		}

		generateOil(event.world, event.rand, event.chunkX, event.chunkZ);
	}

	public void generateOil(World world, Random rand, int chunkX, int chunkZ) {
		for (int i = -GEN_AREA; i <= GEN_AREA; i++) {
			for (int k = -GEN_AREA; k <= GEN_AREA; k++) {
				int cx = chunkX + i;
				int cz = chunkZ + k;
				if (cx == chunkX && cz == chunkZ) {
					continue;
				}
				if (world.getChunkProvider().chunkExists(cx, cz)) {
					Random r = getRandom(world, cx, cz);
					doPopulate(world, r, cx, cz, false);
				}
			}
		}

		Random r = getRandom(world, chunkX, chunkZ);
		doPopulate(world, r, chunkX, chunkZ, true);
	}

	private Random getRandom(World world, int chunkX, int chunkZ) {
		return new Random(world.getSeed() + chunkX + chunkZ * 90373);
	}

	public void doPopulate(World world, Random rand, int chunkX, int chunkZ, boolean modifyNeighbors) {
		// shift to world coordinates
		int x = chunkX * 16 + 8;
		int z = chunkZ * 16 + 8;

		BiomeGenBase biome = world.getBiomeGenForCoords(x + 8, z + 8);

		// Do not generate oil in the End or Nether
		if (excludedBiomes.contains(biome.biomeID)) {
			return;
		}

		// Generate a surface oil lake
		if (surfaceDepositBiomes.contains(biome.biomeID) && rand.nextDouble() <= 0.02) { // 2%
			int lakeX = rand.nextInt(10) + 2 + x;
			int lakeZ = rand.nextInt(10) + 2 + z;
			int lakeY = world.getTopSolidOrLiquidBlock(lakeX, lakeZ) - 1;

			int blockId = world.getBlockId(lakeX, lakeY, lakeZ);
			if (blockId == biome.topBlock) {
				generateSurfaceDeposit(world, rand, lakeX, lakeY, lakeZ, 2 + rand.nextInt(5), modifyNeighbors);
			}
		}

		double bonus = surfaceDepositBiomes.contains(biome.biomeID) ? 1.5 : 1;
		boolean mediumDeposit = rand.nextDouble() <= 0.0015 * bonus; // 0.15% 
		boolean largeDeposit = rand.nextDouble() <= 0.0005 * bonus; // 0.05%

		if (BuildCraftCore.debugMode) {
			largeDeposit = rand.nextDouble() < 0.01;
		}

		// Generate a large cave deposit
		if (mediumDeposit || largeDeposit) {
//			System.out.printf("Gen: %d, %d, %d\n", x, z, seed);

			int wellX = x, wellZ = z;
			int wellY = 20 + rand.nextInt(10);
			int baseY;
			if (largeDeposit && BuildCraftEnergy.spawnOilSprings && (BuildCraftCore.debugMode || rand.nextDouble() <= 0.25)) {
				baseY = 0;
			} else {
				baseY = wellY;
			}

			int radius = 0;

			if (largeDeposit) {
				radius = 8 + rand.nextInt(9);
			} else if (mediumDeposit) {
				radius = 4 + rand.nextInt(4);
			}

			int radiusSq = radius * radius;

			for (int poolX = -radius; poolX <= radius; poolX++) {
				for (int poolY = -radius; poolY <= radius; poolY++) {
					for (int poolZ = -radius; poolZ <= radius; poolZ++) {
						int distance = poolX * poolX + poolY * poolY + poolZ * poolZ;

						if (distance <= radiusSq && canReplaceInChunk(chunkX, chunkZ, poolX + wellX, poolZ + wellZ, modifyNeighbors)) {
							world.setBlock(poolX + wellX, poolY + wellY, poolZ + wellZ, BuildCraftEnergy.oilStill.blockID);
						}
					}
				}
			}

			Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
			int groundLevel = getGroundLevel(chunk);
			if (groundLevel <= 0) {
				groundLevel = getTopBlock(world, chunk, wellX, wellZ);
				setGroundLevel(chunk, groundLevel);
			}
			if (largeDeposit) {
				int lakeRadius = 20 + rand.nextInt(20);
				if (BuildCraftCore.debugMode) {
					lakeRadius += 40;
				}
				generateSurfaceDeposit(world, rand, wellX, groundLevel, wellZ, lakeRadius, modifyNeighbors);
			} else if (mediumDeposit) {
				generateSurfaceDeposit(world, rand, wellX, groundLevel, wellZ, 5 + rand.nextInt(5), modifyNeighbors);
			}

			int wellHeight = 4;
			if (largeDeposit) {
				wellHeight = 16;
			}
			int maxHeight = Math.min(groundLevel + wellHeight, world.getActualHeight() - 1);

			if (world.getBlockId(wellX, baseY, wellZ) == Block.bedrock.blockID) {
				if (BuildCraftEnergy.spawnOilSprings) {
					world.setBlock(wellX, baseY, wellZ, BuildCraftCore.springBlock.blockID, 1, 3);
				}
			}
			for (int y = baseY + 1; y <= maxHeight; ++y) {
				world.setBlock(wellX, y, wellZ, BuildCraftEnergy.oilStill.blockID);
			}
		}
	}

	public void generateSurfaceDeposit(World world, Random rand, int x, int y, int z, int radius, boolean modifyNeighbors) {
		int depth = rand.nextDouble() < 0.5 ? 1 : 2;

		setOilForLake(world, x, y, z, depth);

		int chunkX = x / 16;
		int chunkZ = z / 16;

		for (int w = 1; w <= radius; ++w) {
			float proba = (float) (radius - w + 4) / (float) (radius + 4);

			for (int d = -w; d <= w; ++d) {
				if (canReplaceInChunk(chunkX, chunkZ, x + d, z + w, modifyNeighbors)) {
					setOilWithProba(world, rand, proba, x + d, y, z + w, depth);
				}
				if (canReplaceInChunk(chunkX, chunkZ, x + d, z - w, modifyNeighbors)) {
					setOilWithProba(world, rand, proba, x + d, y, z - w, depth);
				}
				if (canReplaceInChunk(chunkX, chunkZ, x + w, z + d, modifyNeighbors)) {
					setOilWithProba(world, rand, proba, x + w, y, z + d, depth);
				}
				if (canReplaceInChunk(chunkX, chunkZ, x - w, z + d, modifyNeighbors)) {
					setOilWithProba(world, rand, proba, x - w, y, z + d, depth);
				}
			}
		}

		for (int dx = x - radius; dx <= x + radius; ++dx) {
			for (int dz = z - radius; dz <= z + radius; ++dz) {
				if (!canReplaceInChunk(x / 16, z / 16, dx, dz, modifyNeighbors)) {
					continue;
				}
				if (isOil(world, dx, y - 1, dz)) {
					continue;
				}
				if (isOilSurrounded(world, dx, y - 1, dz)) {
					setOilForLake(world, dx, y, dz, depth);
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
		for (int d = -1; d <= 1; ++d) {
			if (isOil(world, x + d, y, z) || isOil(world, x - d, y, z) || isOil(world, x, y, z + d) || isOil(world, x, y, z - d)) {
				return true;
			}
		}
		return false;
	}

	private boolean isOilSurrounded(World world, int x, int y, int z) {
		return isOil(world, x + 1, y, z)
				&& isOil(world, x - 1, y, z)
				&& isOil(world, x, y, z + 1)
				&& isOil(world, x, y, z - 1);
	}

	private void setOilWithProba(World world, Random rand, float proba, int x, int y, int z, int depth) {
		if (!world.blockExists(x, y, z)) {
			return;
		}
		if (rand.nextFloat() <= proba && world.getBlockId(x, y - 2, z) != 0) {
			if (isOilAdjacent(world, x, y - 1, z)) {
				setOilForLake(world, x, y, z, depth);
			}
		}
	}

	private void setOilForLake(World world, int x, int y, int z, int depth) {
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
		NBTTagCompound nbt = chunkData.remove(event.getChunk());
		if (nbt != null && nbt.hasKey(CHUNK_TAG)) {
			event.getData().setTag(CHUNK_TAG, nbt.getTag(CHUNK_TAG));
		}
	}

	@ForgeSubscribe
	public void loadChunk(ChunkDataEvent.Load event) {
		chunkData.put(event.getChunk(), event.getData());
	}

	private NBTTagCompound getChunkBuildcraftData(Chunk chunk) {
		NBTTagCompound nbt = chunkData.get(chunk);
		if (nbt == null) {
			nbt = new NBTTagCompound();
			chunkData.put(chunk, nbt);
		}
		NBTTagCompound buildcraftData = nbt.getCompoundTag(CHUNK_TAG);
		if (!nbt.hasKey(CHUNK_TAG)) {
			nbt.setCompoundTag(CHUNK_TAG, buildcraftData);
		}
		return buildcraftData;
	}

	private boolean canReplaceInChunk(int chunkX, int chunkZ, int x, int z, boolean modifyNeighbors) {
		if (modifyNeighbors) {
			return true;
		}
		return chunkX == x / 16 && chunkZ == z / 16;
	}

	private void setGroundLevel(Chunk chunk, int height) {
		NBTTagCompound nbt = getChunkBuildcraftData(chunk);
		nbt.setInteger(GROUND_TAG, height);
	}

	private int getGroundLevel(Chunk chunk) {
		NBTTagCompound nbt = getChunkBuildcraftData(chunk);
		return nbt.getInteger(GROUND_TAG);
	}

	private int getTopBlock(World world, Chunk chunk, int x, int z) {
		int y = chunk.getTopFilledSegment() + 15;

		int trimmedX = x & 15;
		int trimmedZ = z & 15;

		for (; y > 0; --y) {
			int blockId = chunk.getBlockID(trimmedX, y, trimmedZ);
			Block block = Block.blocksList[blockId];
			if (blockId == 0 || block.blockMaterial == Material.leaves) {
				continue;
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
