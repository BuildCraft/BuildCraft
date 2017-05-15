package buildcraft.energy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.BCModules;

import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.fluid.BCFluidBlock;
import buildcraft.lib.fluid.FluidManager;

public class BCEnergyFluids {
    public static BCFluid[] crudeOil;
    /** All 3 fuels (no residue) */
    public static BCFluid[] oilDistilled;
    /** The 3 heaviest components (fuelLight, fuelDense and oilResidue) */
    public static BCFluid[] oilHeavy;
    /** The 2 lightest fuels (no dense fuel) */
    public static BCFluid[] fuelMixedLight;
    /** The 2 heaviest fuels (no gaseous fuel) */
    public static BCFluid[] fuelMixedHeavy;
    /** The 2 heaviest products (fuelDense and oilResidue) */
    public static BCFluid[] oilDense;

    // End products in order from least to most dense
    public static BCFluid[] fuelGaseous;
    public static BCFluid[] fuelLight;
    public static BCFluid[] fuelDense;
    public static BCFluid[] oilResidue;

    public static BCFluid tar;

    public static final List<BCFluid> allFluids = new ArrayList<>();

    public static void preInit() {
        int[][] data = { //@formatter:off
            // Tabular form of all the fluid values
            // density, viscosity, boil, spread,  tex_light, tex_dark
            {      900,      2000,    3,      6, 0x50_50_50, 0x05_05_05 },// Crude Oil
            {     1200,      4000,    3,      4, 0x10_0F_10, 0x42_10_42 },// Residue
            {      850,      1800,    3,      6, 0xA0_8F_1F, 0x42_35_20 },// Heavy Oil
            {      950,      1600,    3,      5, 0x87_6E_77, 0x42_24_24 },// Dense Oil
            {      750,      1400,    2,      8, 0xE4_BF_78, 0xA4_8F_00 },// Distilled Oil
            {      600,       800,    2,      7, 0xFF_AF_3F, 0xE0_7F_00 },// Dense Fuel
            {      700,      1000,    2,      7, 0xF2_A7_00, 0xC4_87_00 },// Mixed Heavy Fuels
            {      400,       600,    1,      8, 0xFF_FF_30, 0xE4_CF_00 },// Light Fuel
            {      650,       900,    1,      9, 0xF6_D7_00, 0xC4_B7_00 },// Mixed Light Fuels
            {      300,       500,    0,     10, 0xFA_F6_30, 0xE0_D9_00 },// Gas Fuel
        };//@formatter:on
        if (BCModules.FACTORY.isLoaded()) {
            int index = 0;

            // Add all of the fluid states
            crudeOil = defineFluids(data[index++], "oil", MapColor.BLACK);
            oilResidue = defineFluids(data[index++], "oil_residue", MapColor.OBSIDIAN);
            oilHeavy = defineFluids(data[index++], "oil_heavy", MapColor.BROWN);
            oilDense = defineFluids(data[index++], "oil_dense", MapColor.BROWN);
            oilDistilled = defineFluids(data[index++], "oil_distilled", MapColor.YELLOW);
            fuelDense = defineFluids(data[index++], "fuel_dense", MapColor.YELLOW);
            fuelMixedHeavy = defineFluids(data[index++], "fuel_mixed_heavy", MapColor.YELLOW);
            fuelLight = defineFluids(data[index++], "fuel_light", MapColor.YELLOW);
            fuelMixedLight = defineFluids(data[index++], "fuel_mixed_light", MapColor.YELLOW);
            fuelGaseous = defineFluids(data[index++], "fuel_gaseous", MapColor.YELLOW);
        } else {
            crudeOil = new BCFluid[] { defineFluid(data[0], 0, "oil", MapColor.BLACK) };
            oilResidue = new BCFluid[0];
            oilHeavy = new BCFluid[0];
            oilDense = new BCFluid[0];
            oilDistilled = new BCFluid[0];
            fuelDense = new BCFluid[0];
            fuelMixedHeavy = new BCFluid[0];
            fuelLight = new BCFluid[] { defineFluid(data[7], 0, "fuel_light", MapColor.YELLOW) };
            fuelMixedLight = new BCFluid[0];
            fuelGaseous = new BCFluid[0];
        }
    }

    private static BCFluid[] defineFluids(int[] data, String name, MapColor mapColour) {
        BCFluid[] arr = new BCFluid[3];
        for (int h = 0; h < 3; h++) {
            arr[h] = defineFluid(data, h, name, mapColour);
        }
        return arr;
    }

    private static BCFluid defineFluid(int[] data, int heat, String name, MapColor mapColour) {
        final int density = data[0];
        final int baseViscocity = data[1];
        final int boilPoint = data[2];
        final int baseQuanta = data[3];
        final int texLight = data[4];
        final int texDark = data[5];

        String fullName = name + (heat == 0 ? "" : "_heat_" + heat);
        int tempAdjustedViscocity = baseViscocity * (4 - heat) / 4;
        int boilAdjustedDensity = density * (heat >= boilPoint ? -1 : 1);

        String fluidTexture = "buildcraftenergy:blocks/fluids/" + name + "_heat_" + heat;
        BCFluid def = new BCFluid(fullName, new ResourceLocation(fluidTexture + "_still"), new ResourceLocation(fluidTexture + "_flow"));
        def.setBlockName(name + "_heat_" + heat);
        def.setMapColour(mapColour);
        def.setFlamable(true);
        def.setHeat(heat);
        def.setUnlocalizedName(name);
        def.setTemperature(300 + 20 * heat);
        def.setViscosity(tempAdjustedViscocity);
        def.setDensity(boilAdjustedDensity);
        def.setGaseous(def.getDensity() < 0);
        def.setColour(texLight, texDark);
        def.setHeatable(true);
        FluidManager.register(def, true);

        BCFluidBlock block = (BCFluidBlock) def.getBlock();
        // Distance that the fluid will travel: 1->16
        // Higher heat values travel a little further
        block.setQuantaPerBlock(baseQuanta + (baseQuanta > 6 ? heat : heat / 2));

        allFluids.add(def);
        return def;
    }

}
