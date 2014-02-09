package buildcraft.energy.worldgen;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenOcean;
import net.minecraftforge.common.BiomeDictionary;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BiomeGenOilOcean extends BiomeGenOcean {

	protected static final BiomeGenBase.Height height_OilOcean = new BiomeGenBase.Height(0.1F, 0.2F);
	
	public static BiomeGenOilOcean makeBiome(int id) {
		BiomeGenOilOcean biome = new BiomeGenOilOcean(id);
		BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.WATER);
		OilPopulate.INSTANCE.excessiveBiomes.add(biome.biomeID);
		OilPopulate.INSTANCE.surfaceDepositBiomes.add(biome.biomeID);
		return biome;
	}

	private BiomeGenOilOcean(int id) {
		super(id);
		setBiomeName("Ocean Oil Field");
		setColor(112);
		setHeight(height_Oceans);
	}
}
