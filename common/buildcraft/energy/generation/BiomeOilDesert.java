/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy.generation;

import net.minecraft.world.biome.BiomeDesert;

public final class BiomeOilDesert extends BiomeDesert {
    public static final BiomeOilDesert INSTANCE = new BiomeOilDesert();

    public BiomeOilDesert() {
        super(new BiomeProperties("Desert Oil Field").setRainDisabled().setTemperature(2.0F).setBaseHeight(0.125F));
        setRegistryName("oil_desert");
    }

//    protected static final BiomeDesert.Height height_OilDesert = new Biome.Height(0.1F, 0.2F);
//
//    private BiomeOilDesert(int id) {
//        super(id);
//        setColor(16421912);
//        setBiomeName("Desert Oil Field");
//        setDisableRain();
//        setTemperatureRainfall(2.0F, 0.0F);
//        setHeight(height_OilDesert);
//    }
//
//    public static BiomeOilDesert makeBiome(int id) {
//        BiomeOilDesert biome = new BiomeOilDesert(id);
//        BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.SANDY);
//        OilPopulateOld.INSTANCE.excessiveBiomes.add(biome.biomeID);
//        OilPopulateOld.INSTANCE.surfaceDepositBiomes.add(biome.biomeID);
//        return biome;
//    }
}
