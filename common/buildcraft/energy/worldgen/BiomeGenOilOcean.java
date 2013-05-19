package buildcraft.energy.worldgen;

import net.minecraft.world.biome.BiomeGenOcean;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BiomeGenOilOcean extends BiomeGenOcean {

	public BiomeGenOilOcean(int id) {
		super(id);
		setBiomeName("Ocean Oil Field");
		setColor(112);
		setMinMaxHeight(-1.0F, 0.4F);
	}
}
