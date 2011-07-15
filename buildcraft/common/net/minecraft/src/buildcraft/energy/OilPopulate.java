package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.World;
import net.minecraft.src.forge.IBiomePopulator;

public class OilPopulate implements IBiomePopulator {

	@Override
	public void populate(World world, BiomeGenBase biomegenbase, int x, int z) {
		if (biomegenbase == BiomeGenBase.desert && world.rand.nextFloat() > 0.95) {
			int startX = world.rand.nextInt(10) + 2;
			int startZ = world.rand.nextInt(10) + 2;
			
			for (int j = 128; j > 65; --j) {
				int i = startX + x;
				int k = startZ + z;

				if (world.getBlockId(i, j, k) != 0) {
					if (world.getBlockId(i, j, k) == Block.sand.blockID) {
						System.out.println("POPULATE " + i + ", "
								+ k);
						
						for (int dx = -1; dx <= 1; dx++) {
							for (int dz = -1; dz <= 1; dz++) {
								world.setBlock(i + dx, j, k + dz, 0);
								world.setBlock(i + dx, j - 1, k + dz,
										BuildCraftEnergy.oilStill.blockID);
							}
						}
					}
					
					break;
				}
			}			
		}
	}

}
