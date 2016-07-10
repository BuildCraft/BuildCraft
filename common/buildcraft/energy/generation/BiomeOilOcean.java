/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy.generation;

import net.minecraft.world.biome.BiomeOcean;

public final class BiomeOilOcean extends BiomeOcean {
    public static final BiomeOilOcean INSTANCE = new BiomeOilOcean();

    public BiomeOilOcean() {
        super(new BiomeProperties("Ocean Oil Field").setRainDisabled().setBaseHeight(-1.0F).setHeightVariation(0.1F));
        setRegistryName("oil_ocean");
    }

//    protected static final BiomeGenBase.Height height_OilOcean = new BiomeGenBase.Height(0.1F, 0.2F);
//
//    private BiomeOilOcean(int id) {
//        super(id);
//        setBiomeName("Ocean Oil Field");
//        setColor(112);
//        setHeight(height_Oceans);
//    }
//
//    public static BiomeOilOcean makeBiome(int id) {
//        BiomeOilOcean biome = new BiomeOilOcean(id);
//        BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.WATER);
//        OilPopulateOld.INSTANCE.excessiveBiomes.add(biome.biomeID);
//        OilPopulateOld.INSTANCE.surfaceDepositBiomes.add(biome.biomeID);
//        return biome;
//    }
}
