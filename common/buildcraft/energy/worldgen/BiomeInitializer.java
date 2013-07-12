package buildcraft.energy.worldgen;

import buildcraft.BuildCraftEnergy;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.terraingen.WorldTypeEvent;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BiomeInitializer {

	public BiomeInitializer() {
	}

	@ForgeSubscribe
	public void initBiomes(WorldTypeEvent.InitBiomeGens event) {
		if (BuildCraftEnergy.biomeOilDesert != null) {
			event.newBiomeGens[0] = new GenLayerAddOilDesert(event.seed, 1500L, event.newBiomeGens[0]);
			event.newBiomeGens[1] = new GenLayerAddOilDesert(event.seed, 1500L, event.newBiomeGens[1]);
			event.newBiomeGens[2] = new GenLayerAddOilDesert(event.seed, 1500L, event.newBiomeGens[2]);
		}
		if (BuildCraftEnergy.biomeOilOcean != null) {
			event.newBiomeGens[0] = new GenLayerAddOilOcean(event.seed, 1500L, event.newBiomeGens[0]);
			event.newBiomeGens[1] = new GenLayerAddOilOcean(event.seed, 1500L, event.newBiomeGens[1]);
			event.newBiomeGens[2] = new GenLayerAddOilOcean(event.seed, 1500L, event.newBiomeGens[2]);
		}

//		int range = GenLayerBiomeReplacer.OFFSET_RANGE;
//		Random rand = new Random(event.seed);
//		double xOffset = rand.nextInt(range) - (range / 2);
//		double zOffset = rand.nextInt(range) - (range / 2);
//		double noiseScale = GenLayerAddOilOcean.NOISE_FIELD_SCALE;
//		double noiseThreshold = GenLayerAddOilOcean.NOISE_FIELD_THRESHOLD;
//		for (int x = -5000; x < 5000; x += 128) {
//			for (int z = -5000; z < 5000; z += 128) {
//				if (SimplexNoise.noise((x + xOffset) * noiseScale, (z + zOffset) * noiseScale) > noiseThreshold) {
//					System.out.printf("Oil Biome: %d, %d\n", x, z);
//				}
//			}
//		}
	}
}
