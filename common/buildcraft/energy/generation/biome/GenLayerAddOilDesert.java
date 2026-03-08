/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation.biome;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate.Parameter;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import net.minecraftforge.common.Tags.Biomes;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;

public class GenLayerAddOilDesert extends GenLayerBiomeReplacer {
    private static final double NOISE_FIELD_SCALE = 0.001;
    private static final double NOISE_FIELD_THRESHOLD = 0.7;

    // private static final List<BiomeDictionary.Type> REQUIRED_TYPES = Arrays.asList(BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY, BiomeDictionary.Type.SANDY);
    private static final List<TagKey<Biome>> REQUIRED_TYPES = Arrays.asList(Biomes.IS_HOT, Biomes.IS_DRY, Biomes.IS_SANDY);

    public GenLayerAddOilDesert(long worldSeed, long seed/*, GenLayer parent*/) {
        super(worldSeed, seed, /*parent, */NOISE_FIELD_SCALE, NOISE_FIELD_THRESHOLD, getOilBiomeId());
    }

    public static Holder<Biome> getOilBiomeId() {
//        return Biome.getIdForBiome(BiomeOilDesert.INSTANCE);
        return ForgeRegistries.BIOMES.getHolder(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_DESERT).orElse(null);
    }

    @Override
    protected Holder<Biome> getHolder() {
        return getOilBiomeId();
    }

    @Override
    protected ParameterPoint getParameterPoint() {
        return new ParameterPoint(
                Parameter.span(0.55F, 1.0F),
                Parameter.span(-1, -0.35F),
                Parameter.span(-0.19F, 0.03F),
                Parameter.span(-1, 0.5F),
                Parameter.point(0),
                Parameter.span(-1, 1),
                0L
        );
    }

    @Override
    protected boolean canReplaceBiome(Holder<Biome> biome) {
        if (biome == null) {
            return false;
        }
//        return BiomeDictionary.getTypes(biome).containsAll(REQUIRED_TYPES);
        return REQUIRED_TYPES.stream().anyMatch(biome::is);
    }
}
