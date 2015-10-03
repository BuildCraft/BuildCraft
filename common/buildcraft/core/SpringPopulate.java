/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpringPopulate {

    @SubscribeEvent
    public void populate(PopulateChunkEvent.Post event) {

        boolean doGen = TerrainGen.populate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ, event.hasVillageGenerated,
                PopulateChunkEvent.Populate.EventType.CUSTOM);

        if (!doGen) {
            event.setResult(Result.ALLOW);
            return;
        }

        // shift to world coordinates
        int worldX = event.chunkX << 4;
        int worldZ = event.chunkZ << 4;

        doPopulate(event.world, event.rand, worldX, worldZ);
    }

    private void doPopulate(World world, Random random, int x, int z) {

        // A spring will be generated every 40th chunk.
        if (random.nextFloat() > 0.025f) {
            return;
        }

        // Do not generate water in the End or the Nether
        BiomeGenBase biomegenbase = world.getWorldChunkManager().getBiomeGenerator(new BlockPos(x, 10, z));
        if (biomegenbase.biomeID == BiomeGenBase.sky.biomeID || biomegenbase.biomeID == BiomeGenBase.hell.biomeID) {
            return;
        }

        int posX = x + random.nextInt(16);
        int posZ = z + random.nextInt(16);

        for (int i = 0; i < 5; i++) {
            BlockPos pos = new BlockPos(posX, i, posZ);
            Block candidate = world.getBlockState(pos).getBlock();

            if (candidate != Blocks.bedrock) {
                continue;
            }
            world.setBlockState(pos.up(), BuildCraftCore.springBlock.getDefaultState());

            for (int j = i + 2; j < world.getActualHeight() - 10; j++) {
                BlockPos above = pos.up(j);
                if (!boreToSurface(world, above)) {
                    if (world.isAirBlock(above)) {
                        world.setBlockState(above, Blocks.water.getDefaultState());
                    }

                    break;
                }
            }

            break;
        }
    }

    private boolean boreToSurface(World world, BlockPos pos) {
        if (world.isAirBlock(pos)) {
            return false;
        }

        Block existing = world.getBlockState(pos).getBlock();
        if (existing != Blocks.stone && existing != Blocks.dirt && existing != Blocks.gravel && existing != Blocks.grass) {
            return false;
        }

        world.setBlockState(pos, Blocks.water.getDefaultState());
        return true;
    }
}
