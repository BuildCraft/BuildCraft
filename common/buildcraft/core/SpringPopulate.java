package buildcraft.core;

import java.util.Random;

import buildcraft.BuildCraftCore;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;

public class SpringPopulate implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		
		// shift to world coordinates
		int x = chunkX << 4;
		int z = chunkZ << 4;

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
						world.setBlockWithNotify(posX, j, posZ, Block.waterStill.blockID);
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
		
		world.setBlockWithNotify(x, y, z, Block.waterStill.blockID);
		return true;
	}
}
