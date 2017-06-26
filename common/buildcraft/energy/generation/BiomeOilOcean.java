/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeOcean;

import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.energy.BCEnergy;

public final class BiomeOilOcean extends BiomeOcean {
    @GameRegistry.ObjectHolder(BCEnergy.MODID + ":oil_ocean")
    public static final Biome INSTANCE = null;

    public BiomeOilOcean() {
        super(
                new BiomeProperties("Ocean Oil Field")
                        .setBaseHeight(-1.0F)
                        .setHeightVariation(0.1F)
        );
        setRegistryName("oil_ocean");
    }
}
