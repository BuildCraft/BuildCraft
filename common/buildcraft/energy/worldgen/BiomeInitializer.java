/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy.worldgen;

import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.energy.BuildCraftEnergy;

public class BiomeInitializer {

    public BiomeInitializer() {}

    @SubscribeEvent
    public void initBiomes(WorldTypeEvent.InitBiomeGens event) {
        int i;
        if (BuildCraftEnergy.biomeOilDesert != null) {
            for (i = 0; i < event.newBiomeGens.length; i++) {
                event.newBiomeGens[i] = new GenLayerAddOilDesert(event.seed, 1500L, event.newBiomeGens[i]);
            }
        }
        if (BuildCraftEnergy.biomeOilOcean != null) {
            for (i = 0; i < event.newBiomeGens.length; i++) {
                event.newBiomeGens[i] = new GenLayerAddOilOcean(event.seed, 1500L, event.newBiomeGens[i]);
            }
        }
    }
}
