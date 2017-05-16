package buildcraft.energy.generation;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import java.util.Arrays;

import com.google.common.collect.ImmutableList;

public class GenLayerAddOilDesert extends GenLayerBiomeReplacer {
    protected static final double NOISE_FIELD_SCALE = 0.001;
    protected static final double NOISE_FIELD_THRESHOLD = 0.7;
    
    private static final ImmutableList<BiomeDictionary.Type> requiredTypes;
    
    static {
        requiredTypes = ImmutableList.of(Type.HOT, Type.DRY, Type.SANDY);
    }

    public GenLayerAddOilDesert(long worldSeed, long seed, GenLayer parent) {
        super(worldSeed, seed, parent, NOISE_FIELD_SCALE, NOISE_FIELD_THRESHOLD, Biome.getIdForBiome(BiomeOilDesert.INSTANCE));
    }

    @Override
    protected boolean canReplaceBiome(int biomeId) {
        Biome biome = Biome.getBiomeForId(biomeId);
        if (biome == null) {
            throw new IllegalArgumentException("Couldn't find a biome with an ID of " + biomeId);
        }
        return BiomeDictionary.getTypes(biome).containsAll(requiredTypes);
    }
}
