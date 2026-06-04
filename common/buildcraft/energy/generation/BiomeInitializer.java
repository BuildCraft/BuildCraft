/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import net.minecraft.world.gen.layer.GenLayer;

import net.minecraftforge.event.terraingen.WorldTypeEvent;
// STUB(R.Chen): // @SubscribeEvent — TODO(R.Chen): port to Fabric event removed — port to Fabric events

import buildcraft.energy.BCEnergyConfig;

public class BiomeInitializer {
    // @SubscribeEvent — TODO(R.Chen): port to Fabric event
    public void initBiomeGens(WorldTypeEvent.InitBiomeGens event) {
        // STUB(R.Chen): Forge GenLayer biome injection (InitBiomeGens) has no Fabric 1.20.1 equivalent.
        // Oil-biome worldgen is deferred to a Fabric BiomeModification rewrite — Phase 10.
    }
}
