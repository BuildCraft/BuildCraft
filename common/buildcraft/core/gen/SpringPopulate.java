/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.enums.EnumSpring;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.core.BCCoreBlocks;

public class SpringPopulate {

    @SubscribeEvent
    public void populate(PopulateChunkEvent.Post event) {

        World world = event.getWorld();
        Random rand = event.getRand();
        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();
        boolean doGen = TerrainGen.populate(event.getGen(), world, rand, chunkX, chunkZ, event.isHasVillageGenerated(), PopulateChunkEvent.Populate.EventType.CUSTOM);

        if (!doGen || !EnumSpring.WATER.canGen) {
            event.setResult(Result.ALLOW);
            return;
        }

        // shift to world coordinates
        int worldX = chunkX << 4;
        int worldZ = chunkZ << 4;

        doPopulate(world, rand, worldX, worldZ);
    }

    private static void doPopulate(World world, Random random, int x, int z) {
        int dimId = world.provider.getDimension();
        // No water springs will generate in the Nether or End.
        if (dimId == -1 || dimId == 1) {
            return;
        }

        // A spring will be generated every 40th chunk.
        if (random.nextFloat() > 0.025f) {
            return;
        }

        int posX = x + random.nextInt(16);
        int posZ = z + random.nextInt(16);

        for (int i = 0; i < 5; i++) {
            BlockPos pos = new BlockPos(posX, i, posZ);
            Block candidate = world.getBlockState(pos).getBlock();

            if (candidate != Blocks.BEDROCK) {
                continue;
            }

            // Handle flat bedrock maps
            int y = i > 0 ? i : i - 1;

            IBlockState springState = BCCoreBlocks.spring.getDefaultState();
            springState = springState.withProperty(BuildCraftProperties.SPRING_TYPE, EnumSpring.WATER);

            world.setBlockState(new BlockPos(posX, y, posZ), springState);

            for (int j = y + 2; j < world.getActualHeight(); j++) {
                if (world.isAirBlock(new BlockPos(posX, j, posZ))) {
                    break;
                } else {
                    world.setBlockState(new BlockPos(posX, j, posZ), Blocks.WATER.getDefaultState());
                }
            }

            break;
        }
    }
}
