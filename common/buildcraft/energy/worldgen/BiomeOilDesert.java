package buildcraft.energy.worldgen;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenDesert;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BiomeOilDesert extends BiomeGenDesert {

	public BiomeOilDesert(int id) {
		super(id);
		setColor(16421912);
		setBiomeName("Desert Oil Field");
		setDisableRain();
		setTemperatureRainfall(2.0F, 0.0F);
		setMinMaxHeight(0.1F, 0.2F);
	}

	@Override
	public void decorate(World par1World, Random par2Random, int par3, int par4) {
	}
}
