package ct.buildcraft.energy.generation.features;

import java.util.List;

import com.mojang.serialization.Codec;

import ct.buildcraft.lib.misc.data.Box;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class OilGenFeature extends Feature<OilFeatureConfiguration>{
	
    /** The distance that oil generation will be checked to see if their structures overlap with the currently
     * generating chunk. This should be large enough that all oil generation can fit inside this radius. If this number
     * is too big then oil generation will be slightly slower */
    private static final int MAX_CHUNK_RADIUS = 5;
    

	public OilGenFeature(Codec<OilFeatureConfiguration> p_65786_) {
		super(p_65786_);
	}

	@Override
	public boolean place(FeaturePlaceContext<OilFeatureConfiguration> pfc) {
        WorldGenLevel world = pfc.level();
        BlockPos orginPos = pfc.origin();
        ChunkPos chunkPos = world.getChunk(orginPos).getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        
        OilGenerator.config = pfc.config();

/*        if (world.getLevelType() == LevelType.FLAT) {
            if (DEBUG_OILGEN_BASIC) {
                BCLog.logger.info(
                    "[energy.oilgen] Not generating oil in " + world + " chunk " + chunkX + ", " + chunkZ
                        + " because it's LevelType is FLAT."
                );
            }
            return;
        }*/
/*        boolean isExcludedDimension = BCEnergyConfig.excludedDimensions.contains(world.dimensionTypeId().location());
        if (isExcludedDimension == BCEnergyConfig.excludedDimensionsIsBlackList) {
            if (DEBUG_OILGEN_BASIC) {
                BCLog.logger.info(
                    "[energy.oilgen] Not generating oil in " + world + " chunk " + chunkX + ", " + chunkZ
                        + " because it's dimension is disabled."
                );
            }
            return;
        }
*/
//        world.profiler.startSection("bc_oil");
        int count = 0;
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        world.setBlock(orginPos, Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
        BlockPos min = new BlockPos(x, world.dimensionType().minY(), z);
        Box box = new Box(min, min.offset(15, world.getHeight(), 15));

        for (int cdx = -MAX_CHUNK_RADIUS; cdx <= MAX_CHUNK_RADIUS; cdx++) {
            for (int cdz = -MAX_CHUNK_RADIUS; cdz <= MAX_CHUNK_RADIUS; cdz++) {
                int cx = chunkX + cdx;
                int cz = chunkZ + cdz;
//                world.getProfiler().startSection("scan");
                List<OilStructure> structures = OilGenerator.getStructures(world, cx, cz/*, cdx == 0 && cdz == 0*/);
                OilStructure.Spring spring = null;
//                world.getProfiler().endStartSection("gen");
                for (OilStructure struct : structures) {
                    struct.generate(world, box);
                    if (struct instanceof OilStructure.Spring) {
                        spring = (OilStructure.Spring) struct;
                    }
                }
                if (spring != null && box.contains(spring.pos)) {
                    
                    for (OilStructure struct : structures) {
                        count += struct.countOilBlocks();
                    }
                    spring.generate(world, count);
                }
//                world.getProfiler().pop();;
            }
        }
//        world.getProfiler().pop();
		return count > 0;
    }



}
