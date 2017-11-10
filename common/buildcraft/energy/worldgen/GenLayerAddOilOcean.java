/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.worldgen;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.layer.GenLayer;

import buildcraft.BuildCraftEnergy;

public class GenLayerAddOilOcean extends GenLayerBiomeReplacer {

	public static final double NOISE_FIELD_SCALE = 0.0005;
	public static final double NOISE_FIELD_THRESHOLD = 0.9;

	public GenLayerAddOilOcean(final long worldSeed, final long seed, final GenLayer parent) {
		super(worldSeed, seed, parent, NOISE_FIELD_SCALE, NOISE_FIELD_THRESHOLD, BuildCraftEnergy.biomeOilOcean.biomeID);
	}

	@Override
	protected boolean canReplaceBiome(int biomeId) {
		return biomeId == BiomeGenBase.ocean.biomeID;
	}
}
