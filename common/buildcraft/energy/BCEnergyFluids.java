package buildcraft.energy;

import java.util.*;

import com.google.common.math.IntMath;
import net.minecraft.block.material.MapColor;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.BCModules;

import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.fluid.BCFluidBlock;
import buildcraft.lib.fluid.FluidManager;

public class BCEnergyFluids {
    public static BCFluid[] crudeOil;
    /**
     * All 3 fuels (no residue)
     */
    public static BCFluid[] oilDistilled;
    /**
     * The 3 heaviest components (fuelLight, fuelDense and oilResidue)
     */
    public static BCFluid[] oilHeavy;
    /**
     * The 2 lightest fuels (no dense fuel)
     */
    public static BCFluid[] fuelMixedLight;
    /**
     * The 2 heaviest fuels (no gaseous fuel)
     */
    public static BCFluid[] fuelMixedHeavy;
    /**
     * The 2 heaviest products (fuelDense and oilResidue)
     */
    public static BCFluid[] oilDense;

    // End products in order from least to most dense
    public static BCFluid[] fuelGaseous;
    public static BCFluid[] fuelLight;
    public static BCFluid[] fuelDense;
    public static BCFluid[] oilResidue;

    public static BCFluid tar;

    public static final List<BCFluid> allFluids = new ArrayList<>();

    public static void preInit() {
        if (BCModules.FACTORY.isLoaded()) {
            crudeOil = defineFluids("oil", 4000, 4000, 3, 6, 0x50_50_50, 0x05_05_05);
            oilResidue = defineFluids("oil_residue", 6000, 8000, 3, 4, 0x10_0F_10, 0x21_0F_42);
            oilHeavy = defineFluids("oil_heavy", 4000, 4000, 2, 6, 0xA0_8F_1F, 0x42_35_20);
            oilDense = defineFluids("oil_dense", 5000, 5000, 3, 5, 0x87_6E_77, 0x42_24_24);
            oilDistilled = defineFluids("oil_distilled", 3000, 3500, 2, 8, 0xE4_BF_78, 0xA4_8F_00);
            fuelDense = defineFluids("fuel_dense", 2000, 5000, 2, 7, 0xFF_AF_3F, 0xE0_7F_00);
            fuelMixedHeavy = defineFluids("fuel_mixed_heavy", 1200, 700, 2, 7, 0xF2_A7_00, 0xC4_87_00);
            fuelLight = defineFluids("fuel_light", 1000, 900, 1, 8, 0xFF_FF_30, 0xE4_CF_00);
            fuelMixedLight = defineFluids("fuel_mixed_light", 800, 700, 1, 9, 0xF6_D7_00, 0xC4_B7_00);
            fuelGaseous = defineFluids("fuel_gaseous", 300, 600, 0, 10, 0xFA_F6_30, 0xE0_D9_00);
        } else {
            crudeOil = new BCFluid[] {defineFluid("oil", 4000, 4000, 0, 3, 6, 0x50_50_50, 0x05_05_05)};
            oilResidue = new BCFluid[0];
            oilHeavy = new BCFluid[0];
            oilDense = new BCFluid[0];
            oilDistilled = new BCFluid[0];
            fuelDense = new BCFluid[0];
            fuelMixedHeavy = new BCFluid[0];
            fuelLight = new BCFluid[] {defineFluid("fuel_light", 1000, 900, 0, 1, 8, 0xFF_FF_30, 0xE4_CF_00)};
            fuelMixedLight = new BCFluid[0];
            fuelGaseous = new BCFluid[0];
        }
    }

    private static BCFluid[] defineFluids(String name, int density, int baseViscocity, int boilPoint, int baseQuanta, int lightColor, int darkColor) {
        BCFluid[] arr = new BCFluid[3];
        for (int h = 0; h < 3; h++) {
            arr[h] = defineFluid(name, density, baseViscocity, h, boilPoint, baseQuanta, lightColor, darkColor);
        }
        return arr;
    }

    private static BCFluid defineFluid(String name, int density, int baseViscocity, int heat, int boilPoint, int baseQuanta, int lightColor, int darkColor) {
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
        def.setColour(lightColor, darkColor);
        def.setMapColour(
                Arrays.stream(MapColor.COLORS)
                        .filter(Objects::nonNull)
                        .filter(mapColor -> mapColor.colorValue != 0)
                        .min(Comparator.comparingInt(mapColor ->
                                IntMath.pow((mapColor.colorValue >> 16 & 0xFF) - (darkColor >> 16 & 0xFF), 2) +
                                        IntMath.pow((mapColor.colorValue >> 8 & 0xFF) - (darkColor >> 8 & 0xFF), 2) +
                                        IntMath.pow((mapColor.colorValue & 0xFF) - (darkColor & 0xFF), 2)
                        ))
                        .orElseThrow(IllegalArgumentException::new)
        );
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
