/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDesert;

import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.energy.BCEnergy;

public final class BiomeOilDesert extends BiomeDesert {
    @GameRegistry.ObjectHolder(BCEnergy.MODID + ":oil_desert")
    public static final Biome INSTANCE = null;

    public BiomeOilDesert() {
        super(new BiomeProperties("Desert Oil Field").setBaseHeight(0.125F).setHeightVariation(0.05F)
            .setTemperature(2.0F).setRainfall(0.0F).setRainDisabled());
        setRegistryName("oil_desert");
    }
}
