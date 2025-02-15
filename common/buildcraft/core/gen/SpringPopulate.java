/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.gen;

// Calen: never used in 1.12.2
public class SpringPopulate {

//    @SubscribeEvent
//    public void populate(PopulateChunkEvent.Post event) {
//
//        World world = event.getWorld();
//        Random rand = event.getRand();
//        int chunkX = event.getChunkX();
//        int chunkZ = event.getChunkZ();
//        boolean doGen = TerrainGen.populate(event.getGen(), world, rand, chunkX, chunkZ, event.isHasVillageGenerated(), PopulateChunkEvent.Populate.EventType.CUSTOM);
//
//        if (!doGen || !EnumSpring.WATER.canGen) {
//            event.setResult(Result.ALLOW);
//            return;
//        }
//
//        // shift to world coordinates
//        int worldX = chunkX << 4;
//        int worldZ = chunkZ << 4;
//
//        doPopulate(world, rand, worldX, worldZ);
//    }
//
//    private static void doPopulate(World world, Random random, int x, int z) {
//        int dimId = world.provider.getDimension();
//        // No water springs will generate in the Nether or End.
//        if (dimId == -1 || dimId == 1) {
//            return;
//        }
//
//        // A spring will be generated every 40th chunk.
//        if (random.nextFloat() > 0.025f) {
//            return;
//        }
//
//        int posX = x + random.nextInt(16);
//        int posZ = z + random.nextInt(16);
//
//        for (int i = 0; i < 5; i++) {
//            BlockPos pos = new BlockPos(posX, i, posZ);
//            Block candidate = world.getBlockState(pos).getBlock();
//
//            if (candidate != Blocks.BEDROCK) {
//                continue;
//            }
//
//            // Handle flat bedrock maps
//            int y = i > 0 ? i : i - 1;
//
//            IBlockState springState = BCCoreBlocks.spring.getDefaultState();
//            springState = springState.withProperty(BuildCraftProperties.SPRING_TYPE, EnumSpring.WATER);
//
//            world.setBlockState(new BlockPos(posX, y, posZ), springState);
//
//            for (int j = y + 2; j < world.getHeight(); j++) {
//                if (world.isAirBlock(new BlockPos(posX, j, posZ))) {
//                    break;
//                } else {
//                    world.setBlockState(new BlockPos(posX, j, posZ), Blocks.WATER.getDefaultState());
//                }
//            }
//
//            break;
//        }
//    }
}
