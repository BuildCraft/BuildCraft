/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy.worldgen;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.enums.EnumSpring;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.core.BuildCraftCore;
import buildcraft.energy.BuildCraftEnergy;

public final class OilPopulate {

    public static final OilPopulate INSTANCE = new OilPopulate();
    public static final EventType EVENT_TYPE = EnumHelper.addEnum(EventType.class, "BUILDCRAFT_OIL", new Class[0], new Object[0]);
    private static final byte LARGE_WELL_HEIGHT = 16;
    private static final byte MEDIUM_WELL_HEIGHT = 6;
    public final Set<Integer> excessiveBiomes = new HashSet<Integer>();
    public final Set<Integer> surfaceDepositBiomes = new HashSet<Integer>();
    public final Set<Integer> excludedBiomes = new HashSet<Integer>();

    private enum GenType {
        LARGE,
        MEDIUM,
        LAKE,
        NONE
    }

    private OilPopulate() {
        // BuildCraftCore.debugWorldgen = true;
    }

    @SubscribeEvent
    public void populate(PopulateChunkEvent.Pre event) {
        boolean doGen = TerrainGen.populate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ, event.hasVillageGenerated,
                EVENT_TYPE);

        if (!doGen) {
            event.setResult(Result.ALLOW);
            return;
        }

        generateOil(event.world, event.rand, event.chunkX, event.chunkZ);
    }

    public void generateOil(World world, Random rand, int chunkX, int chunkZ) {

        // shift to world coordinates
        int x = chunkX * 16 + 8 + rand.nextInt(16);
        int z = chunkZ * 16 + 8 + rand.nextInt(16);

        BiomeGenBase biome = world.getBiomeGenForCoords(new BlockPos(x, 0, z));

        // Do not generate oil in the End or Nether
        if (excludedBiomes.contains(biome.biomeID)) {
            return;
        }

        boolean oilBiome = surfaceDepositBiomes.contains(biome.biomeID);

        double bonus = oilBiome ? 3.0 : 1.0;
        bonus *= BuildCraftEnergy.oilWellScalar;
        if (excessiveBiomes.contains(biome.biomeID)) {
            bonus *= 30.0;
        } else if (BuildCraftCore.debugWorldgen) {
            bonus *= 20.0;
        }
        GenType type = GenType.NONE;
        if (rand.nextDouble() <= 0.0004 * bonus) {
            // 0.04%
            type = GenType.LARGE;
        } else if (rand.nextDouble() <= 0.001 * bonus) {
            // 0.1%
            type = GenType.MEDIUM;
        } else if (oilBiome && rand.nextDouble() <= 0.02 * bonus) {
            // 2%
            type = GenType.LAKE;
        }

        if (type == GenType.NONE) {
            return;
        }

        // Find ground level
        int groundLevel = getTopBlock(world, x, z);
        if (groundLevel < 5) {
            return;
        }

        double deviation = surfaceDeviation(world, x, groundLevel, z, 8);
        if (deviation > 0.45) {
            return;
        }

        // Generate a Well
        if (type == GenType.LARGE || type == GenType.MEDIUM) {
            int wellX = x;
            int wellZ = z;

            int wellHeight = MEDIUM_WELL_HEIGHT;
            if (type == GenType.LARGE) {
                wellHeight = LARGE_WELL_HEIGHT;
            }
            int maxHeight = groundLevel + wellHeight;
            if (maxHeight >= world.getActualHeight() - 1) {
                return;
            }

            // Generate a spherical cave deposit
            int wellY = 20 + rand.nextInt(10);

            int radius;
            if (type == GenType.LARGE) {
                radius = 8 + rand.nextInt(9);
            } else {
                radius = 4 + rand.nextInt(4);
            }

            int radiusSq = radius * radius;

            for (int poolX = -radius; poolX <= radius; poolX++) {
                for (int poolY = -radius; poolY <= radius; poolY++) {
                    for (int poolZ = -radius; poolZ <= radius; poolZ++) {
                        int distance = poolX * poolX + poolY * poolY + poolZ * poolZ;

                        if (distance <= radiusSq) {
                            BlockPos pos = new BlockPos(poolX + wellX, poolY + wellY, poolZ + wellZ);
                            world.setBlockState(pos, BuildCraftEnergy.blockOil.getDefaultState(), distance == radiusSq ? 3 : 2);
                        }
                    }
                }
            }

            // Generate Lake around Spout
            int lakeRadius;
            if (type == GenType.LARGE) {
                lakeRadius = 25 + rand.nextInt(20);
                // if (BuildCraftCore.debugMode) {
                // lakeRadius += 40;
                // }
            } else {
                lakeRadius = 5 + rand.nextInt(10);
            }
            generateSurfaceDeposit(world, rand, biome, wellX, groundLevel, wellZ, lakeRadius);

            boolean makeSpring = type == GenType.LARGE && BuildCraftEnergy.spawnOilSprings && BuildCraftCore.springBlock != null
                && (BuildCraftCore.debugWorldgen || rand.nextDouble() <= 0.25);

            // Generate Spout
            int baseY;
            if (makeSpring) {
                baseY = 0;
            } else {
                baseY = wellY;
            }

            BlockPos well = new BlockPos(wellX, baseY, wellZ);

            if (makeSpring && world.getBlockState(well).getBlock() == Blocks.bedrock) {
                IBlockState state = BuildCraftCore.springBlock.getDefaultState();
                state = state.withProperty(BuildCraftProperties.SPRING_TYPE, EnumSpring.OIL);
                world.setBlockState(well, state, 3);
            }
            IBlockState oil = BuildCraftEnergy.blockOil.getDefaultState();
            for (int y = 1; y <= maxHeight - baseY; ++y) {
                world.setBlockState(well.up(y), oil, 3);
            }

            if (type == GenType.LARGE) {
                for (int y = 0; y <= maxHeight - wellHeight / 2 - wellY; ++y) {
                    world.setBlockState(well.up(y).west(), oil, 3);
                    world.setBlockState(well.up(y).east(), oil, 3);
                    world.setBlockState(well.up(y).north(), oil, 3);
                    world.setBlockState(well.up(y).south(), oil, 3);
                }
            }

        } else if (type == GenType.LAKE) {
            // Generate a surface oil lake
            int lakeX = x;
            int lakeZ = z;
            int lakeY = groundLevel;
            BlockPos lake = new BlockPos(x, groundLevel, z);

            Block block = world.getBlockState(lake).getBlock();
            if (block == biome.topBlock) {
                generateSurfaceDeposit(world, rand, biome, lakeX, lakeY, lakeZ, 5 + rand.nextInt(10));
            }
        }
    }

    public void generateSurfaceDeposit(World world, Random rand, int x, int y, int z, int radius) {
        BiomeGenBase biome = world.getBiomeGenForCoords(new BlockPos(x, y, z));
        generateSurfaceDeposit(world, rand, biome, x, y, z, radius);
    }

    private void generateSurfaceDeposit(World world, Random rand, BiomeGenBase biome, int x, int y, int z, int radius) {
        int depth = rand.nextDouble() < 0.5 ? 1 : 2;

        // Center
        setOilColumnForLake(world, biome, x, y, z, depth, 2);

        // Generate tendrils, from the center outward
        for (int w = 1; w <= radius; ++w) {
            float proba = (float) (radius - w + 4) / (float) (radius + 4);

            setOilWithProba(world, biome, rand, proba, x, y, z + w, depth);
            setOilWithProba(world, biome, rand, proba, x, y, z - w, depth);
            setOilWithProba(world, biome, rand, proba, x + w, y, z, depth);
            setOilWithProba(world, biome, rand, proba, x - w, y, z, depth);

            for (int i = 1; i <= w; ++i) {
                setOilWithProba(world, biome, rand, proba, x + i, y, z + w, depth);
                setOilWithProba(world, biome, rand, proba, x + i, y, z - w, depth);
                setOilWithProba(world, biome, rand, proba, x + w, y, z + i, depth);
                setOilWithProba(world, biome, rand, proba, x - w, y, z + i, depth);

                setOilWithProba(world, biome, rand, proba, x - i, y, z + w, depth);
                setOilWithProba(world, biome, rand, proba, x - i, y, z - w, depth);
                setOilWithProba(world, biome, rand, proba, x + w, y, z - i, depth);
                setOilWithProba(world, biome, rand, proba, x - w, y, z - i, depth);
            }
        }

        // Fill in holes
        for (int dx = x - radius; dx <= x + radius; ++dx) {
            for (int dz = z - radius; dz <= z + radius; ++dz) {
                if (isOil(world, dx, y, dz)) {
                    continue;
                }
                if (isOilSurrounded(world, dx, y, dz)) {
                    setOilColumnForLake(world, biome, dx, y, dz, depth, 2);
                }
            }
        }
    }

    private boolean isReplaceableFluid(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return (block instanceof BlockStaticLiquid || block instanceof BlockFluidBase || block instanceof IFluidBlock) && block
                .getMaterial() != Material.lava;
    }

    private boolean isOil(World world, int x, int y, int z) {
        Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        return block == BuildCraftEnergy.blockOil;
    }

    @SuppressWarnings("unchecked")
    private boolean isReplaceableForLake(World world, BiomeGenBase biome, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (world.isAirBlock(pos)) {
            return true;
        }

        Block block = world.getBlockState(pos).getBlock();

        if (block == biome.fillerBlock || block == biome.topBlock) {
            return true;
        }

        if (!block.getMaterial().blocksMovement()) {
            return true;
        }

        if (block.isReplaceableOreGen(world, pos, BlockHelper.forBlock(Blocks.stone))) {
            return true;
        }

        if (block instanceof BlockFlower) {
            return true;
        }

        if (!block.isOpaqueCube()) {
            return true;
        }

        return false;
    }

    private boolean isOilAdjacent(World world, int x, int y, int z) {
        return isOil(world, x + 1, y, z) || isOil(world, x - 1, y, z) || isOil(world, x, y, z + 1) || isOil(world, x, y, z - 1);
    }

    private boolean isOilSurrounded(World world, int x, int y, int z) {
        return isOil(world, x + 1, y, z) && isOil(world, x - 1, y, z) && isOil(world, x, y, z + 1) && isOil(world, x, y, z - 1);
    }

    private void setOilWithProba(World world, BiomeGenBase biome, Random rand, float proba, int x, int y, int z, int depth) {
        if (rand.nextFloat() <= proba && !world.isAirBlock(new BlockPos(x, y - depth - 1, z))) {
            if (isOilAdjacent(world, x, y, z)) {
                setOilColumnForLake(world, biome, x, y, z, depth, 3);
            }
        }
    }

    private void setOilColumnForLake(World world, BiomeGenBase biome, int x, int y, int z, int depth, int update) {
        if (isReplaceableForLake(world, biome, x, y + 1, z)) {
            if (!world.isAirBlock(new BlockPos(x, y + 2, z))) {
                return;
            }
            BlockPos pos = new BlockPos(x, y, z);
            if (isReplaceableFluid(world, pos) || world.isSideSolid(pos.down(), EnumFacing.UP)) {
                world.setBlockState(pos, BuildCraftEnergy.blockOil.getDefaultState(), update);
            } else {
                return;
            }
            if (!world.isAirBlock(pos.up())) {
                world.setBlockToAir(pos.up());
            }

            for (int d = 1; d <= depth - 1; d++) {
                BlockPos down = pos.down(d);
                if (isReplaceableFluid(world, down) || !world.isSideSolid(down.down(), EnumFacing.UP)) {
                    return;
                }
                world.setBlockState(down, BuildCraftEnergy.blockOil.getDefaultState(), 2);
            }
        }
    }

    private int getTopBlock(World world, int x, int z) {
        BlockPos pos = new BlockPos(x, 0, z);
        Chunk chunk = world.getChunkFromBlockCoords(pos);
        int y = chunk.getTopFilledSegment() + 15;
        pos = pos.up(y);

        int trimmedX = x & 15;
        int trimmedZ = z & 15;

        for (; y > 0; --y) {
            Block block = chunk.getBlock(trimmedX, y, trimmedZ);

            if (block.isAir(world, pos)) {
                continue;
            }

            if (block instanceof BlockStaticLiquid) {
                return y;
            }

            if (block instanceof BlockFluidBase) {
                return y;
            }

            if (block instanceof IFluidBlock) {
                return y;
            }

            if (!block.getMaterial().blocksMovement()) {
                continue;
            }

            if (block instanceof BlockFlower) {
                continue;
            }

            return y - 1;
        }

        return -1;
    }

    private double surfaceDeviation(World world, int x, int y, int z, int radius) {
        int diameter = radius * 2;
        double centralTendancy = y;
        double deviation = 0;
        for (int i = 0; i < diameter; i++) {
            for (int k = 0; k < diameter; k++) {
                deviation += getTopBlock(world, x - radius + i, z - radius + k) - centralTendancy;
            }
        }
        return Math.abs(deviation / centralTendancy);
    }
}
