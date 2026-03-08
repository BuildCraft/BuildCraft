package buildcraft.energy.generation.structure;

import com.mojang.serialization.Codec;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OilFeatureConfiguration implements IFeatureConfig {
    public static final Codec<OilFeatureConfiguration> CODEC = Codec.unit(() -> OilFeatureConfiguration.INSTANCE);
    public static final OilFeatureConfiguration INSTANCE = new OilFeatureConfiguration();

    public static final Map<ChunkPos, Info> cache = new ConcurrentHashMap<>();

    public static class Info {
        public SharedSeedRandom oilRand;
        public int xForGen, zForGen;
        public OilGenerator.GenType type;

        public Info(OilGenerator.GenType type, SharedSeedRandom oilRand, int xForGen, int zForGen) {
            this.type = type;
            this.oilRand = oilRand;
            this.xForGen = xForGen;
            this.zForGen = zForGen;
        }
    }

    public void add(ChunkPos chunkPos, Info info) {
        cache.put(chunkPos, info);
    }

    public Info get(ChunkPos chunkPos) {
        return cache.remove(chunkPos);
    }
}
