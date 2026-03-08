/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation.biome;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;

public class GenLayerAddOilOcean extends GenLayerBiomeReplacer {
    private static final double NOISE_FIELD_SCALE = 0.0005;
    private static final double NOISE_FIELD_THRESHOLD = 0.9;

    private static final List<BiomeDictionary.Type> REQUIRED_TYPES = Arrays.asList(BiomeDictionary.Type.OCEAN);
//    private static final List<INamedTag<Biome>> REQUIRED_TYPES = Arrays.asList(BiomeTags.IS_OCEAN);

    public GenLayerAddOilOcean(long worldSeed, long seed/*, GenLayer parent*/) {
        super(worldSeed, seed, /*parent, */NOISE_FIELD_SCALE, NOISE_FIELD_THRESHOLD, getOilBiomeId());
    }

    public static Biome getOilBiomeId() {
//        return Biome.getIdForBiome(BiomeOilOcean.INSTANCE);
        return ForgeRegistries.BIOMES.getValue(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_OCEAN.getRegistryName());
    }

    @Override
    protected Biome getHolder() {
        return getOilBiomeId();
    }

    @Override
    protected boolean canReplaceBiome(RegistryKey<Biome> biome) {
        if (biome == null) {
            return false;
        }
        return BiomeDictionary.getTypes(biome).containsAll(REQUIRED_TYPES);
    }
}
