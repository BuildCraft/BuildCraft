/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BiomeInitializer {
    @SubscribeEvent
    public void initBiomeGens(WorldTypeEvent.InitBiomeGens event) {
        GenLayer[] newBiomeGens = event.getNewBiomeGens().clone();
        for (int i = 0; i < newBiomeGens.length; i++) {
            newBiomeGens[i] = new GenLayerAddOilOcean(event.getSeed(), 1500L, newBiomeGens[i]);
            newBiomeGens[i] = new GenLayerAddOilDesert(event.getSeed(), 1500L, newBiomeGens[i]);
        }
        event.setNewBiomeGens(newBiomeGens);
    }
}
