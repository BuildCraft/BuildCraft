/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy.generation;

import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BiomeInitializer {
    @SubscribeEvent
    public void initBiomeGens(WorldTypeEvent.InitBiomeGens event) {
        if (BiomeOilOcean.INSTANCE != null) { // TODO: are those check necessary?
            for (int i = 0; i < event.getNewBiomeGens().length; i++) {
                event.getNewBiomeGens()[i] = new GenLayerAddOilOcean(event.getSeed(), 1500L, event.getNewBiomeGens()[i]);
            }
        }
        if (BiomeOilDesert.INSTANCE != null) {
            for (int i = 0; i < event.getNewBiomeGens().length; i++) {
                event.getNewBiomeGens()[i] = new GenLayerAddOilDesert(event.getSeed(), 1500L, event.getNewBiomeGens()[i]);
            }
        }
    }
}
