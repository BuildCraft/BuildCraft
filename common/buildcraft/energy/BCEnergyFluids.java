package buildcraft.energy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;

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
        int[][] colours = {//
            { 0x50_50_50, 0x05_05_05 }, // Crude Oil
            { 0x10_0F_10, 0x42_10F_42 },// Residue
            { 0xA0_8F_1F, 0x42_35_20 },// Heavy Oil
            { 0x87_6E_77, 0x42_24_24 },// Dense Oil
            { 0xE4_BF_78, 0xA4_8F_00 },// Distilled Oil
            { 0xFF_AF_3F, 0xE0_7F_00 },// Dense Fuel
            { 0xF2_A7_00, 0xC4_87_00 },// Mixed Dense + Light Fuel
            { 0xFF_FF_30, 0xE4_CF_00 },// Light Fuel
            { 0xF6_D7_00, 0xC4_B7_00 },// Mixed Light + Gas Fuel
            { 0xFA_F6_30, 0xE0_D9_00 },// Gas Fuel
            { 0x3F_3F_3F, 0x30_30_30 },// Tar
        };

        int index = 0;

        // Add all of the fluid states
        crudeOil = defineFluids("oil", 4000, 4000, 3, 6, colours[index++]);
        oilResidue = defineFluids("oil_residue", 6000, 8000, 3, 4, colours[index++]);
        oilHeavy = defineFluids("oil_heavy", 4000, 4000, 2, 6, colours[index++]);
        oilDense = defineFluids("oil_dense", 5000, 5000, 3, 5, colours[index++]);
        oilDistilled = defineFluids("oil_distilled", 3000, 3500, 2, 8, colours[index++]);
        fuelDense = defineFluids("fuel_dense", 2000, 5000, 2, 7, colours[index++]);
        fuelMixedHeavy = defineFluids("fuel_mixed_heavy", 1200, 700, 2, 7, colours[index++]);
        fuelLight = defineFluids("fuel_light", 1000, 900, 1, 8, colours[index++]);
        fuelMixedLight = defineFluids("fuel_mixed_light", 800, 700, 1, 9, colours[index++]);
        fuelGaseous = defineFluids("fuel_gaseous", 300, 600, 0, 10, colours[index++]);
    }

    private static BCFluid[] defineFluids(String name, int density, int baseViscocity, int boilPoint, int baseQuanta, int[] texColours) {
        BCFluid[] arr = new BCFluid[3];
        for (int h = 0; h < 3; h++) {
            arr[h] = defineFluid(name, density, baseViscocity, h, boilPoint, baseQuanta, texColours);
        }
        return arr;
    }

    private static BCFluid defineFluid(String name, int density, int baseViscocity, int heat, int boilPoint, int baseQuanta, int[] texColours) {
        String fullName = name + (heat == 0 ? "" : "_heat_" + heat);
        int tempAdjustedViscocity = baseViscocity * (4 - heat) / 4;
        int boilAdjustedDensity = density * (heat >= boilPoint ? -1 : 1);

        String fluidTexture = "buildcraftenergy:blocks/fluids/" + name + "_heat_" + heat;
        BCFluid def = new BCFluid(fullName, new ResourceLocation(fluidTexture + "_still"), new ResourceLocation(fluidTexture + "_flow"));
        def.setBlockName(name + "_heat_" + heat);
        def.setFlamable(true);
        def.setHeat(heat);
        def.setUnlocalizedName(name);
        def.setTemperature(300 + 20 * heat);
        def.setViscosity(tempAdjustedViscocity);
        def.setDensity(boilAdjustedDensity);
        def.setGaseous(def.getDensity() < 0);
        def.setColour(texColours[0], texColours[1]);
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
