package buildcraft.energy.generation;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;

import net.minecraftforge.common.BiomeDictionary;

public class GenLayerAddOilOcean extends GenLayerBiomeReplacer {
    public static final double NOISE_FIELD_SCALE = 0.0005;
    public static final double NOISE_FIELD_THRESHOLD = 0.9;

    public GenLayerAddOilOcean(long worldSeed, long seed, GenLayer parent) {
        super(worldSeed, seed, parent, NOISE_FIELD_SCALE, NOISE_FIELD_THRESHOLD, Biome.getIdForBiome(BiomeOilOcean.INSTANCE));
    }

    @Override
    protected boolean canReplaceBiome(int biomeId) {
        Biome biome = Biome.getBiomeForId(biomeId);
        if (biome == null) {
            throw new IllegalArgumentException("Couldn't find a biome with an ID of " + biomeId);
        }
        return BiomeDictionary.getTypes(biome).contains(BiomeDictionary.Type.OCEAN);
    }
}
