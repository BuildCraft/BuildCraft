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

public class OilGenFeature extends Feature<OilFeatureConfiguration> {
    /** The distance that oil generation will be checked to see if their structures overlap with the currently
     * generating chunk. This should be large enough that all oil generation can fit inside this radius. If this number
     * is too big then oil generation will be slightly slower */
    private static final int MAX_CHUNK_RADIUS = 5;

    public OilGenFeature(Codec<OilFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OilFeatureConfiguration> pfc) {
        WorldGenLevel world = pfc.level();
        BlockPos originPos = pfc.origin();
        ChunkPos chunkPos = world.getChunk(originPos).getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;

        OilGenerator.config = pfc.config();

        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        BlockPos min = new BlockPos(x, world.dimensionType().minY(), z);
        Box box = new Box(min, min.offset(15, world.getHeight(), 15));
 //       world.setBlock(new BlockPos(x, OilGenerator.seaLevel, z), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);

        boolean generated = false;

        for (int cdx = -MAX_CHUNK_RADIUS; cdx <= MAX_CHUNK_RADIUS; cdx++) {
            for (int cdz = -MAX_CHUNK_RADIUS; cdz <= MAX_CHUNK_RADIUS; cdz++) {
                int cx = chunkX + cdx;
                int cz = chunkZ + cdz;
                List<OilStructure> structures = OilGenerator.getStructures(world, cx, cz);
                OilStructure.Spring spring = null;
                for (OilStructure struct : structures) {
                    if (struct.box.getIntersect(box) != null) {
                        generated = true;
                    }
                    struct.generate(world, box);
                    if (struct instanceof OilStructure.Spring) {
                        spring = (OilStructure.Spring) struct;
                    }
                }
                if (spring != null && box.contains(spring.pos)) {
                    int count = 0;
                    for (OilStructure struct : structures) {
                        count += struct.countOilBlocks();
                    }
                    spring.generate(world, count);
                    generated = true;
                }
            }
        }
        return generated;
    }
}
