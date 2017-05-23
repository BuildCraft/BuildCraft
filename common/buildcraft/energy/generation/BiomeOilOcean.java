/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import net.minecraft.world.biome.BiomeOcean;

public final class BiomeOilOcean extends BiomeOcean {
    public static final BiomeOilOcean INSTANCE = new BiomeOilOcean();

    public BiomeOilOcean() {
        super(
                new BiomeProperties("Ocean Oil Field")
                        .setBaseHeight(-1.0F)
                        .setHeightVariation(0.1F)
        );
        setRegistryName("oil_ocean");
    }
}
