/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import buildcraft.BuildCraftCore;

public class SpringPopulate {

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

	private void doPopulate(World world, Random random, int x, int z) {

		// A spring will be generated every 40th chunk.
		if(random.nextFloat() > 0.025f)
			return;

		// Do not generate water in the End or the Nether
		BiomeGenBase biomegenbase = world.getWorldChunkManager().getBiomeGenAt(x, z);
		if (biomegenbase.biomeID == BiomeGenBase.sky.biomeID || biomegenbase.biomeID == BiomeGenBase.hell.biomeID)
			return;

		int posX = x + random.nextInt(16);
		int posZ = z + random.nextInt(16);

		for(int i = 0; i < 5; i++) {
			int candidate = world.getBlockId(posX, i, posZ);
			if(candidate != Block.bedrock.blockID)
				continue;

			world.setBlock(posX, i + 1, posZ, BuildCraftCore.springBlock.blockID);
			for(int j = i + 2; j < world.getActualHeight() - 10; j++) {
				if(!boreToSurface(world, posX, j, posZ)) {
					if(world.isAirBlock(posX, j, posZ))
						world.setBlock(posX, j, posZ, Block.waterStill.blockID);
					break;
				}
			}
			break;
		}
	}

	private boolean boreToSurface(World world, int x, int y, int z) {
		if(world.isAirBlock(x, y, z))
			return false;

		int existing = world.getBlockId(x, y, z);
		if(existing != Block.stone.blockID
				&& existing != Block.dirt.blockID
				&& existing != Block.gravel.blockID
				&& existing != Block.grass.blockID)
			return false;

		world.setBlock(x, y, z, Block.waterStill.blockID);
		return true;
	}
}
