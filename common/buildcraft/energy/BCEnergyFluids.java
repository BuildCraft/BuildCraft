/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
            // density, viscosity, boil, spread,  tex_light, tex_dark, sticky
            {      900,      2000,    3,      6, 0xC0_75_34, 0x5a_1d_0c, 1 },// Crude Oil -- Raw Chocolate Mix
            {     1200,      4000,    3,      4, 0xd4_82_39, 0xd8_7d_33, 1 },// Residue -- Caramel Excess
            {      850,      1800,    3,      6, 0xd4_82_39, 0x5a_1d_0c, 1 },// Heavy Oil
            {      950,      1600,    3,      5, 0xd4_82_39, 0x30_0e_05, 1 },// Dense Oil
            {      750,      1400,    2,      8, 0xC0_75_34, 0x8a_3d_1c, 0 },// Distilled Oil -- Mixed Chocolates
            {      600,       800,    2,      7, 0x4f_33_2f, 0x30_0e_05, 0 },// Dense Fuel -- Dark Chocolate
            {      700,      1000,    2,      7, 0x88_44_2d, 0x5a_1d_0c, 0 },// Mixed Heavy Fuels
            {      400,       600,    1,      8, 0x9b_61_39, 0x94_59_31, 0 },// Light Fuel -- Milk Chocolate -- COLOURED
            {      650,       900,    1,      9, 0xc0_75_34, 0xb3_68_2c, 0 },// Mixed Light Fuels
            {      300,       500,    0,     10, 0xd6_c9_90, 0xcf_bf_8e, 0 },// Gas Fuel -- White Chocolate
        };//@formatter:on
        if (BCModules.FACTORY.isLoaded()) {
            int index = 0;

            // Add all of the fluid states
            crudeOil = defineFluids(data[index++], "oil");
            oilResidue = defineFluids(data[index++], "oil_residue");
            oilHeavy = defineFluids(data[index++], "oil_heavy");
            oilDense = defineFluids(data[index++], "oil_dense");
            oilDistilled = defineFluids(data[index++], "oil_distilled");
            fuelDense = defineFluids(data[index++], "fuel_dense");
            fuelMixedHeavy = defineFluids(data[index++], "fuel_mixed_heavy");
            fuelLight = defineFluids(data[index++], "fuel_light");
            fuelMixedLight = defineFluids(data[index++], "fuel_mixed_light");
            fuelGaseous = defineFluids(data[index++], "fuel_gaseous");
        } else {
            crudeOil = new BCFluid[] { defineFluid(data[0], 0, "oil") };
            oilResidue = new BCFluid[0];
            oilHeavy = new BCFluid[0];
            oilDense = new BCFluid[0];
            oilDistilled = new BCFluid[0];
            fuelDense = new BCFluid[0];
            fuelMixedHeavy = new BCFluid[0];
            fuelLight = new BCFluid[] { defineFluid(data[7], 0, "fuel_light") };
            fuelMixedLight = new BCFluid[0];
            fuelGaseous = new BCFluid[0];
        }
    }

    private static BCFluid[] defineFluids(int[] data, String name) {
        BCFluid[] arr = new BCFluid[3];
        for (int h = 0; h < 3; h++) {
            arr[h] = defineFluid(data, h, name);
        }
        return arr;
    }

    private static BCFluid defineFluid(int[] data, int heat, String name) {
        final int density = data[0];
        final int baseViscosity = data[1];
        final int boilPoint = data[2];
        final int baseQuanta = data[3];
        final int texLight = data[4];
        final int texDark = data[5];
        final boolean sticky = data[6] == 1;

        String fullName = name + (heat == 0 ? "" : "_heat_" + heat);
        int tempAdjustedViscosity = baseViscosity * (4 - heat) / 4;
        int boilAdjustedDensity = density;// * (heat >= boilPoint ? -1 : 1);

        String fluidTexture = "buildcraftenergy:blocks/fluids/" + name + "_heat_" + heat;
        BCFluid def = new BCFluid(fullName, new ResourceLocation(fluidTexture + "_still"),
            new ResourceLocation(fluidTexture + "_flow"));
        def.setBlockName(name + "_heat_" + heat);
        def.setMapColour(getMapColour(texLight));
        def.setFlammable(true);
        def.setHeat(heat);
        def.setUnlocalizedName(name);
        def.setTemperature(300 + 20 * heat);
        def.setViscosity(tempAdjustedViscosity);
        def.setDensity(boilAdjustedDensity);
        // def.setGaseous(def.getDensity() < 0);
        def.setColour(texLight, texDark);
        def.setHeatable(true);
        FluidManager.register(def);

        BCFluidBlock block = (BCFluidBlock) def.getBlock();
        block.setLightOpacity(3);
        block.setSticky(sticky);
        // Distance that the fluid will travel: 1->16
        // Higher heat values travel a little further
        block.setQuantaPerBlock(baseQuanta + (baseQuanta > 6 ? heat : heat / 2));

        allFluids.add(def);
        return def;
    }

    private static MapColor getMapColour(int colour) {
        MapColor bestColor = MapColor.BLACK;
        int currentDifference = Integer.MAX_VALUE;

        int r = (colour >> 16) & 0xFF;
        int g = (colour >> 8) & 0xFF;
        int b = (colour >> 0) & 0xFF;

        for (MapColor map : MapColor.COLORS) {
            if (map == null || map.colorValue == 0) {
                continue;
            }
            int mr = (map.colorValue >> 16) & 0xFF;
            int mg = (map.colorValue >> 8) & 0xFF;
            int mb = (map.colorValue >> 0) & 0xFF;

            int dr = mr - r;
            int dg = mg - g;
            int db = mb - b;

            int difference = dr * dr + dg * dg * db + db;

            if (difference < currentDifference) {
                currentDifference = difference;
                bestColor = map;
            }
        }
        return bestColor;
    }
}
