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
			event.newBiomeGens[1] = new GenLayerAddOilDesert(1500L, event.newBiomeGens[1]);
		}
	}
}
