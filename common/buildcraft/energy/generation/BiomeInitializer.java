/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import net.minecraft.world.gen.layer.GenLayer;

import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.energy.BCEnergyConfig;

public class BiomeInitializer {
    @SubscribeEvent
    public void initBiomeGens(WorldTypeEvent.InitBiomeGens event) {

        boolean oilOcean = BCEnergyConfig.enableOilOceanBiome && GenLayerAddOilOcean.getOilBiomeId() >= 0;
        boolean oilDesert = BCEnergyConfig.enableOilDesertBiome &&GenLayerAddOilDesert.getOilBiomeId() >= 0;

        if (!oilOcean && !oilDesert) {
            // The biomes aren't registered, so don't bother creating a new array.
            return;
        }

        GenLayer[] newBiomeGens = event.getNewBiomeGens().clone();
        for (int i = 0; i < newBiomeGens.length; i++) {
            if (oilOcean) newBiomeGens[i] = new GenLayerAddOilOcean(event.getSeed(), 1500L, newBiomeGens[i]);
            if (oilDesert) newBiomeGens[i] = new GenLayerAddOilDesert(event.getSeed(), 1500L, newBiomeGens[i]);
        }
        event.setNewBiomeGens(newBiomeGens);
    }
}
