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
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.core.BlockSpring;
import java.util.HashSet;
import java.util.Set;

public class OilPopulate {

	public static final Set<Integer> surfaceDepositBiomes = new HashSet<Integer>();

	static {
		surfaceDepositBiomes.add(BiomeGenBase.desert.biomeID);
	}

	@ForgeSubscribe
	public void populate(PopulateChunkEvent.Post event) {

		boolean doGen = TerrainGen.populate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkX, event.hasVillageGenerated, PopulateChunkEvent.Populate.EventType.CUSTOM);

		if (!doGen) {
			return;
		}

		// shift to world coordinates
		int worldX = event.chunkX << 4;
		int worldZ = event.chunkZ << 4;

		doPopulate(event.world, event.rand, worldX, worldZ);
	}

	public static void doPopulate(World world, Random rand, int x, int z) {
		BiomeGenBase biome = world.getBiomeGenForCoords(x + 16, z + 16);

		// Do not generate oil in the End
		if (biome.biomeID == BiomeGenBase.sky.biomeID || biome.biomeID == BiomeGenBase.hell.biomeID) {
			return;
		}

		if (surfaceDepositBiomes.contains(biome.biomeID) && rand.nextFloat() > 0.97) {
			// Generate a small desert deposit

			int startX = rand.nextInt(10) + 2;
			int startZ = rand.nextInt(10) + 2;

			for (int j = 128; j > 65; --j) {
				int i = startX + x;
				int k = startZ + z;

				int blockId = world.getBlockId(i, j, k);
				if (blockId != 0 && blockId == Block.sand.blockID) {
					generateSurfaceDeposit(world, rand, i, j, k, 3);
				}
				break;
			}
		}

		boolean mediumDeposit = rand.nextDouble() <= (0.15 / 100.0);
		boolean largeDeposit = rand.nextDouble() <= (0.005 / 100.0);

		if (BuildCraftCore.debugMode && x == 0 && z == 0) {
			largeDeposit = true;
		}

		if (mediumDeposit || largeDeposit) {
			// Generate a large cave deposit

			int baseX = x, baseZ = z;
			int wellY = 20 + rand.nextInt(10);
			int baseY;
			if (largeDeposit && (BuildCraftCore.debugMode || rand.nextDouble() <= 0.25)) {
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

						if (distance <= radiusSq) {
							world.setBlock(poolX + baseX, poolY + wellY, poolZ + baseZ, BuildCraftEnergy.oilStill.blockID);
						}
					}
				}
			}

			boolean started = false;

			for (int y = 128; y >= baseY; --y) {
				if (started) {
					int blockId = world.getBlockId(baseX, y, baseZ);
					if (blockId == Block.bedrock.blockID) {
						world.setBlock(baseX, y, baseZ, BuildCraftCore.springBlock.blockID, 1, 2);
						break;
					}
					world.setBlock(baseX, y, baseZ, BuildCraftEnergy.oilStill.blockID);
				} else {
					int blockId = world.getBlockId(baseX, y, baseZ);
					if (blockId != 0 && blockId != Block.leaves.blockID && blockId != Block.wood.blockID && blockId != Block.grass.blockID) {
						started = true;

						if (largeDeposit) {
							generateSurfaceDeposit(world, rand, baseX, y, baseZ, 20 + rand.nextInt(20));
						} else if (mediumDeposit) {
							generateSurfaceDeposit(world, rand, baseX, y, baseZ, 5 + rand.nextInt(5));
						}

						int ymax = 0;

						if (largeDeposit) {
							ymax = (y + 30 < 128 ? y + 30 : 128);
						} else if (mediumDeposit) {
							ymax = (y + 4 < 128 ? y + 4 : 128);
						}

						for (int h = y + 1; h <= ymax; ++h) {
							world.setBlock(baseX, h, baseZ, BuildCraftEnergy.oilStill.blockID);
						}

					}
				}
			}
		}
	}

	public static void generateSurfaceDeposit(World world, Random rand, int x, int y, int z, int radius) {
		setOilWithProba(world, rand, 1, x, y, z, true);

		for (int w = 1; w <= radius; ++w) {
			float proba = (float) (radius - w + 4) / (float) (radius + 4);

			for (int d = -w; d <= w; ++d) {
				setOilWithProba(world, rand, proba, x + d, y, z + w, false);
				setOilWithProba(world, rand, proba, x + d, y, z - w, false);
				setOilWithProba(world, rand, proba, x + w, y, z + d, false);
				setOilWithProba(world, rand, proba, x - w, y, z + d, false);
			}
		}

		for (int dx = x - radius; dx <= x + radius; ++dx) {
			for (int dz = z - radius; dz <= z + radius; ++dz) {

				if (world.getBlockId(dx, y - 1, dz) != BuildCraftEnergy.oilStill.blockID) {
					if (isOil(world, dx + 1, y - 1, dz) && isOil(world, dx - 1, y - 1, dz) && isOil(world, dx, y - 1, dz + 1)
							&& isOil(world, dx, y - 1, dz - 1)) {
						setOilWithProba(world, rand, 1.0F, dx, y, dz, true);
					}
				}
			}
		}
	}

	private static boolean isOil(World world, int x, int y, int z) {
		return (world.getBlockId(x, y, z) == BuildCraftEnergy.oilStill.blockID || world.getBlockId(x, y, z) == BuildCraftEnergy.oilMoving.blockID);
	}

	public static void setOilWithProba(World world, Random rand, float proba, int x, int y, int z, boolean force) {
		if ((rand.nextFloat() <= proba && world.getBlockId(x, y - 2, z) != 0) || force) {
			boolean adjacentOil = false;

			for (int d = -1; d <= 1; ++d) {
				if (isOil(world, x + d, y - 1, z) || isOil(world, x - d, y - 1, z) || isOil(world, x, y - 1, z + d) || isOil(world, x, y - 1, z - d)) {
					adjacentOil = true;
				}
			}

			if (adjacentOil || force) {
				if (world.getBlockId(x, y, z) == Block.waterMoving.blockID || world.getBlockId(x, y, z) == Block.waterStill.blockID || isOil(world, x, y, z)) {

					world.setBlock(x, y, z, BuildCraftEnergy.oilStill.blockID);
				} else {
					world.setBlock(x, y, z, 0);
				}

				world.setBlock(x, y - 1, z, BuildCraftEnergy.oilStill.blockID);
			}
		}
	}
}
