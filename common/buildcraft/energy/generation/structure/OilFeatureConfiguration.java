package buildcraft.energy.generation.structure;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class OilFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<OilFeatureConfiguration> CODEC = Codec.unit(() -> OilFeatureConfiguration.INSTANCE);
    public static final OilFeatureConfiguration INSTANCE = new OilFeatureConfiguration();

//    public static final Map<ChunkPos, Info> cache = new ConcurrentHashMap<>();

    public static class Info {
        public WorldgenRandom oilRand;
        public int xForGen, zForGen;
        public OilGenerator.GenType type;

        public Info(OilGenerator.GenType type, WorldgenRandom oilRand, int xForGen, int zForGen) {
            this.type = type;
            this.oilRand = oilRand;
            this.xForGen = xForGen;
            this.zForGen = zForGen;
        }
    }

//    public static void add(ChunkPos chunkPos, Info info) {
//        cache.put(chunkPos, info);
//    }
//
//    public static Info get(ChunkPos chunkPos) {
//        return cache.remove(chunkPos);
//    }
}
