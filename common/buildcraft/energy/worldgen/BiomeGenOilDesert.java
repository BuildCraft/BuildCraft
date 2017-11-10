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
import net.minecraft.world.biome.BiomeGenDesert;

import net.minecraftforge.common.BiomeDictionary;

public final class BiomeGenOilDesert extends BiomeGenDesert {

	protected static final BiomeGenBase.Height height_OilDesert = new BiomeGenBase.Height(0.1F, 0.2F);

	private BiomeGenOilDesert(int id) {
		super(id);
		setColor(16421912);
		setBiomeName("Desert Oil Field");
		setDisableRain();
		setTemperatureRainfall(2.0F, 0.0F);
		setHeight(height_OilDesert);
	}

	public static BiomeGenOilDesert makeBiome(int id) {
		BiomeGenOilDesert biome = new BiomeGenOilDesert(id);
		BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.SANDY);
		OilPopulate.INSTANCE.excessiveBiomes.add(biome.biomeID);
		OilPopulate.INSTANCE.surfaceDepositBiomes.add(biome.biomeID);
		return biome;
	}
}
