package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.World;
import net.minecraft.src.forge.IBiomePopulator;

public class OilPopulate implements IBiomePopulator {

	int total = 0;
	
	@Override
	public void populate(World world, BiomeGenBase biomegenbase, int x, int z) {
		total++;
		
		if (biomegenbase == BiomeGenBase.desert && world.rand.nextFloat() > 0.95) {
			// Generate a small desert deposit
			
			System.out.println ("SMALL DEPOSIT ON " + x + ", " + z);
			
			int startX = world.rand.nextInt(10) + 2;
			int startZ = world.rand.nextInt(10) + 2;
			
			for (int j = 128; j > 65; --j) {
				int i = startX + x;
				int k = startZ + z;

				if (world.getBlockId(i, j, k) != 0) {
					if (world.getBlockId(i, j, k) == Block.sand.blockID) {						
						for (int dx = -1; dx <= 1; dx++) {
							for (int dz = -1; dz <= 1; dz++) {
								world.setBlockWithNotify(i + dx, j, k + dz, 0);
								world.setBlockWithNotify(i + dx, j - 1, k + dz,
										BuildCraftEnergy.oilStill.blockID);
							}
						}
					}
					
					break;
				}
			}			
		}
		
		if (world.rand.nextFloat() > 0.998) {
			// Generate a large cave deposit
			
			System.out.println ("LARGE DEPOSIT ON " + x + ", " + z);
			
			int cx = x, cy = 5 + world.rand.nextInt(10), cz = z;
			int r = 10 + world.rand.nextInt(5);
			int r2 = r * r;
			
			for (int bx = -r; bx <= r; bx++) {
				for (int by = -r; by <= r; by++) {
					for (int bz = -r; bz <= r; bz++) {						
						int d2 = bx * bx + by * by + bz * bz;
						
						if (d2 <= r2) {
							world.setBlockWithNotify(bx + cx, by + cy, bz + cz,
									BuildCraftEnergy.oilStill.blockID);							
						} 						
					}
				}									
			}
			
			boolean started = false;
			
			for (int y = 128; y >= cy; --y) {
				if (world.getBlockId(cx, y, cz) != 0
						&& world.getBlockId(cx, y, cz) != Block.leaves.blockID
						&& world.getBlockId(cx, y, cz) != Block.wood.blockID
						&& world.getBlockId(cx, y, cz) != Block.grass.blockID) {
					
					started = true;
					
					if (world.getBlockId(cx, y, cz) != Block.waterStill.blockID
							&& world.getBlockId(cx, y, cz) != Block.waterStill.blockID) {
						world.setBlockWithNotify(cx, y, cz, 0);
					} else {
						world.setBlockWithNotify(cx, y, cz,
								BuildCraftEnergy.oilStill.blockID);
					}
				} else if (started) {
					world.setBlockWithNotify(cx, y, cz,
							BuildCraftEnergy.oilStill.blockID);												
				}
			}
			
		}
	}

}
