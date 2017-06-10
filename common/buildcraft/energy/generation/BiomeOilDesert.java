/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import net.minecraft.world.biome.BiomeDesert;

public final class BiomeOilDesert extends BiomeDesert {
    public static final BiomeOilDesert INSTANCE = new BiomeOilDesert();

    public BiomeOilDesert() {
        super(
                new BiomeProperties("Desert Oil Field")
                        .setBaseHeight(0.125F)
                        .setHeightVariation(0.05F)
                        .setTemperature(2.0F)
                        .setRainfall(0.0F)
                        .setRainDisabled()
        );
        setRegistryName("oil_desert");
    }
}
