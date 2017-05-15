package buildcraft.energy.generation;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Arrays;

public class GenLayerAddOilDesert extends GenLayerBiomeReplacer {
    protected static final double NOISE_FIELD_SCALE = 0.001;
    protected static final double NOISE_FIELD_THRESHOLD = 0.7;

    public GenLayerAddOilDesert(long worldSeed, long seed, GenLayer parent) {
        super(worldSeed, seed, parent, NOISE_FIELD_SCALE, NOISE_FIELD_THRESHOLD, Biome.getIdForBiome(BiomeOilDesert.INSTANCE));
    }

    @Override
    protected boolean canReplaceBiome(int biomeId) {
        // noinspection ConstantConditions
        return BiomeDictionary.getTypes(Biome.getBiomeForId(biomeId)).containsAll(
                Arrays.asList(
                        BiomeDictionary.Type.HOT,
                        BiomeDictionary.Type.DRY,
                        BiomeDictionary.Type.SANDY
                )
        );
    }
}
