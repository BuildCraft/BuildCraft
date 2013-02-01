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
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class OilPopulate {

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
		BiomeGenBase biomegenbase = world.getBiomeGenForCoords(x + 16, z + 16);

		// Do not generate oil in the End
		if (biomegenbase.biomeID == BiomeGenBase.sky.biomeID || biomegenbase.biomeID == BiomeGenBase.hell.biomeID) {
			return;
		}

		if (biomegenbase == BiomeGenBase.desert && rand.nextFloat() > 0.97) {
			// Generate a small desert deposit

			int startX = rand.nextInt(10) + 2;
			int startZ = rand.nextInt(10) + 2;

			for (int j = 128; j > 65; --j) {
				int i = startX + x;
				int k = startZ + z;

				if (world.getBlockId(i, j, k) != 0) {
					if (world.getBlockId(i, j, k) == Block.sand.blockID) {
						generateSurfaceDeposit(world, rand, i, j, k, 3);
					}

					break;
				}
			}
		}

		boolean mediumDeposit = rand.nextDouble() <= (0.15 / 100.0);
		boolean largeDeposit = rand.nextDouble() <= (0.005 / 100.0);

		if (BuildCraftCore.debugMode && x == 0 && z == 0) {
			largeDeposit = true;
		}

		if (mediumDeposit || largeDeposit) {
			// Generate a large cave deposit

			int cx = x, cy = 20 + rand.nextInt(10), cz = z;

			int r = 0;

			if (largeDeposit) {
				r = 8 + rand.nextInt(9);
			} else if (mediumDeposit) {
				r = 4 + rand.nextInt(4);
			}

			int r2 = r * r;

			for (int bx = -r; bx <= r; bx++) {
				for (int by = -r; by <= r; by++) {
					for (int bz = -r; bz <= r; bz++) {
						int d2 = bx * bx + by * by + bz * bz;

						if (d2 <= r2) {
							world.setBlockWithNotify(bx + cx, by + cy, bz + cz, BuildCraftEnergy.oilStill.blockID);
						}
					}
				}
			}

			boolean started = false;

			for (int y = 128; y >= cy; --y) {
				if (!started && world.getBlockId(cx, y, cz) != 0 && world.getBlockId(cx, y, cz) != Block.leaves.blockID
						&& world.getBlockId(cx, y, cz) != Block.wood.blockID && world.getBlockId(cx, y, cz) != Block.grass.blockID) {

					started = true;

					if (largeDeposit) {
						generateSurfaceDeposit(world, rand, cx, y, cz, 20 + rand.nextInt(20));
					} else if (mediumDeposit) {
						generateSurfaceDeposit(world, rand, cx, y, cz, 5 + rand.nextInt(5));
					}

					int ymax = 0;

					if (largeDeposit) {
						ymax = (y + 30 < 128 ? y + 30 : 128);
					} else if (mediumDeposit) {
						ymax = (y + 4 < 128 ? y + 4 : 128);
					}

					for (int h = y + 1; h <= ymax; ++h) {
						world.setBlockWithNotify(cx, h, cz, BuildCraftEnergy.oilStill.blockID);
					}

				} else if (started) {
					world.setBlockWithNotify(cx, y, cz, BuildCraftEnergy.oilStill.blockID);
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

					world.setBlockWithNotify(x, y, z, BuildCraftEnergy.oilStill.blockID);
				} else {
					world.setBlockWithNotify(x, y, z, 0);
				}

				world.setBlockWithNotify(x, y - 1, z, BuildCraftEnergy.oilStill.blockID);
			}
		}
	}
}
