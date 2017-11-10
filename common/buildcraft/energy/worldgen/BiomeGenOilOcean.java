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
import net.minecraft.world.biome.BiomeGenOcean;

import net.minecraftforge.common.BiomeDictionary;

public final class BiomeGenOilOcean extends BiomeGenOcean {

	protected static final BiomeGenBase.Height height_OilOcean = new BiomeGenBase.Height(0.1F, 0.2F);

	private BiomeGenOilOcean(int id) {
		super(id);
		setBiomeName("Ocean Oil Field");
		setColor(112);
		setHeight(height_Oceans);
	}

	public static BiomeGenOilOcean makeBiome(int id) {
		BiomeGenOilOcean biome = new BiomeGenOilOcean(id);
		BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.WATER);
		OilPopulate.INSTANCE.excessiveBiomes.add(biome.biomeID);
		OilPopulate.INSTANCE.surfaceDepositBiomes.add(biome.biomeID);
		return biome;
	}
}
