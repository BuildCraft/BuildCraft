package buildcraft.factory.refining;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelFluid;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftEnergy;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IComplexRefineryRecipeManager;
import buildcraft.core.lib.client.sprite.SpriteColourMapper;
import buildcraft.core.lib.fluids.FluidDefinition;
import buildcraft.core.lib.utils.ModelHelper;

public class ComplexRefiningManager {
    public static FluidDefinition[] crudeOil;
    /** All 3 fuels (no residue) */
    public static FluidDefinition[] oilDistilled;
    /** The 3 heaviest components (fuelLight, fuelDense and oilResidue) */
    public static FluidDefinition[] oilHeavy;
    /** The 2 lightest fuels (no dense fuel) */
    public static FluidDefinition[] fuelMixedLight;
    /** The 2 heaviest fuels (no gaseous fuel) */
    public static FluidDefinition[] fuelMixedHeavy;
    /** The 2 heaviest products (fuelDense and oilResidue) */
    public static FluidDefinition[] oilDense;

    // End products in order from least to most dense
    public static FluidDefinition[] fuelGaseous;
    public static FluidDefinition[] fuelLight;
    public static FluidDefinition[] fuelDense;
    public static FluidDefinition[] oilResidue;

    public static FluidDefinition tar;
    public static FluidDefinition steam;

    private static final List<FluidDefinition> allFluids = new ArrayList<>();

    public static void preInit() {
        int[][] colours = {// All colours.
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
            { 0xFF_FF_FF, 0xAF_AF_AF } // Steam
        };

        int index = 0;

        // Add all of the fluid states
        crudeOil = defineFluids("oil", 4000, 4000, 3, 4, colours[index][0], colours[index++][1]);
        oilResidue = defineFluids("oilResidue", 6000, 8000, 3, 4, colours[index][0], colours[index++][1]);
        oilHeavy = defineFluids("oilHeavy", 4000, 4000, 3, 2, colours[index][0], colours[index++][1]);
        oilDense = defineFluids("oilDense", 5000, 5000, 3, 4, colours[index][0], colours[index++][1]);
        oilDistilled = defineFluids("oilDistilled", 3000, 3500, 3, 2, colours[index][0], colours[index++][1]);
        fuelDense = defineFluids("fuelDense", 2000, 5000, 3, 2, colours[index][0], colours[index++][1]);
        fuelMixedHeavy = defineFluids("fuelMixedHeavy", 1200, 700, 3, 2, colours[index][0], colours[index++][1]);
        fuelLight = new FluidDefinition[] {
            // @formatter:off
            defineFluid("fuel",      1000, 900, 0, 1, colours[index][0], colours[index][1]),
            defineFluid("fuelLight", 1000, 900, 1, 1, colours[index][0], colours[index][1]),
            defineFluid("fuelLight", 1000, 900, 2, 1, colours[index][0], colours[index][1]),
            defineFluid("fuelLight", 1000, 900, 3, 1, colours[index][0], colours[index++][1]),
            // @formatter:on
        };
        fuelLight[0].fluid.setHeatable(true);
        fuelMixedLight = defineFluids("fuelMixedLight", 800, 700, 3, 1, colours[index][0], colours[index++][1]);
        fuelGaseous = defineFluids("fuelGaseous", 300, 600, 3, 0, colours[index][0], colours[index++][1]);

        tar = defineFluid("tar", 7000, 8000, 0, 4, colours[index][0], colours[index++][1]);
        steam = defineFluid("steam", 100, 1000, 2, 1, colours[index][0], colours[index++][1]);

        BuildCraftEnergy.oil = crudeOil[0];
        BuildCraftEnergy.oil.block.setDense(true);
        BuildCraftEnergy.fuel = fuelLight[0];
    }

    private static FluidDefinition[] defineFluids(String name, int density, int baseViscocity, int maxHeat, int boilPoint, int texColourLight,
            int texColourDark) {
        FluidDefinition[] arr = new FluidDefinition[maxHeat + 1];
        for (int h = 0; h <= maxHeat; h++) {
            arr[h] = defineFluid(name, density, baseViscocity, h, boilPoint, texColourLight, texColourDark);
        }
        if (maxHeat > 0) arr[0].fluid.setHeatable(true);
        return arr;
    }

    private static FluidDefinition defineFluid(String name, int density, int baseViscocity, int heat, int boilPoint, int texColourLight,
            int texColourDark) {
        String fullName = name + (heat == 0 ? "" : "_heat_" + heat);
        int tempAdjustedViscocity = baseViscocity * (5 - heat) / 5;
        int boilAdjustedDensity = density * (heat >= boilPoint ? -1 : 1);

        FluidDefinition def = new FluidDefinition(fullName, fullName, boilAdjustedDensity, tempAdjustedViscocity, 0xFF_00_00_00 | texColourLight,
                0xFF_00_00_00 | texColourDark);
        if (def.bucket != null && heat != 0) {
            def.bucket.setCreativeTab(null);
        }
        def.fluid.setHeat(heat);
        def.fluid.setUnlocalizedName(name);
        def.fluid.setTemperature(300 + 20 * heat);
        if (heat > 0) def.fluid.setHeatable(true);

        allFluids.add(def);
        return def;
    }

    public static void init() {
        // Add the heatables
        addBiDirectionalHeatExchange(crudeOil, 10, 7);

        addBiDirectionalHeatExchange(oilDistilled, 10, 4);
        addBiDirectionalHeatExchange(oilHeavy, 10, 6);

        addBiDirectionalHeatExchange(oilDense, 10, 6);
        addBiDirectionalHeatExchange(fuelMixedHeavy, 10, 5);
        addBiDirectionalHeatExchange(fuelMixedLight, 10, 4);

        addBiDirectionalHeatExchange(oilResidue, 10, 8);
        addBiDirectionalHeatExchange(fuelDense, 10, 5);
        addBiDirectionalHeatExchange(fuelLight, 10, 4);
        addBiDirectionalHeatExchange(fuelGaseous, 10, 3);

        BuildcraftRecipeRegistry.complexRefinery.addHeatableRecipe(new FluidStack(FluidRegistry.WATER, 10), steam.createFluidStack(10), 0, 2, 3,
                false);

        // single
        final int _oil = 4;
        final int _gas = 32;
        final int _light = 8;
        final int _dense = 2;
        final int _residue = 1;

        // double
        final int _gas_light = 10;
        final int _light_dense = 5;
        final int _dense_residue = 2;

        // triple
        final int _light_dense_residue = 5;
        final int _gas_light_dense = 10;

        // 4 split up
        addDistilationRecipe(crudeOil[1], _oil, fuelGaseous[1], _gas, oilHeavy[1], _light_dense_residue, 12);
        addDistilationRecipe(crudeOil[2], _oil, fuelMixedLight[2], _gas_light, oilDense[2], _dense_residue, 8);
        addDistilationRecipe(crudeOil[3], _oil, oilDistilled[3], _gas_light_dense, oilResidue[3], _residue, 4);

        // 3 split up
        addDistilationRecipe(oilDistilled[1], _gas_light_dense, fuelGaseous[1], _gas, fuelMixedHeavy[1], _light_dense, 6);
        addDistilationRecipe(oilDistilled[2], _gas_light_dense, fuelMixedLight[2], _gas_light, fuelDense[2], _dense, 4);
        addDistilationRecipe(oilHeavy[2], _light_dense_residue, fuelLight[2], _light, oilDense[2], _dense_residue, 4);
        addDistilationRecipe(oilHeavy[3], _light_dense_residue, fuelMixedHeavy[3], _light_dense, oilResidue[3], _residue, 4);

        // 2 split up
        addDistilationRecipe(fuelMixedLight[1], _gas_light, fuelGaseous[1], _gas, fuelLight[1], _light, 6);
        addDistilationRecipe(fuelMixedHeavy[2], _light_dense, fuelLight[2], _light, fuelDense[2], _dense, 6);
        addDistilationRecipe(oilDense[3], _dense_residue, fuelDense[3], _dense, oilResidue[3], _residue, 6);

        addNormalFuel(fuelGaseous[0], _gas, 8, 1);
        addNormalFuel(fuelLight[0], _light, 6, 1);
        addNormalFuel(fuelDense[0], _dense, 4, 1);

        addNormalFuel(fuelMixedLight[0], _gas_light, 6.5, 0.75);
        addNormalFuel(fuelMixedHeavy[0], _light_dense, 4.5, 0.75);
        addDirtyFuel(oilDense[0], _dense_residue, 2, 0.75);

        addNormalFuel(oilDistilled[0], _gas_light_dense, 3.5, 0.5);
        addDirtyFuel(oilHeavy[0], _light_dense_residue, 2.5, 0.5);

        addDirtyFuel(crudeOil[0], _oil, 3, 0.25);
    }

    private static void addBiDirectionalHeatExchange(FluidDefinition[] coldToHot, int amount, int ticks) {
        IComplexRefineryRecipeManager manager = BuildcraftRecipeRegistry.complexRefinery;
        for (int h = 1; h < coldToHot.length; h++) {
            FluidDefinition cold = coldToHot[h - 1];
            FluidDefinition hot = coldToHot[h];
            manager.addHeatableRecipe(cold.createFluidStack(amount), hot.createFluidStack(amount), h - 1, h, ticks, false);
            manager.addCoolableRecipe(hot.createFluidStack(amount), cold.createFluidStack(amount), h, h - 1, ticks, false);
        }
    }

    private static void addDistilationRecipe(FluidDefinition from, int fromAmount, FluidDefinition gas, int gasAmount, FluidDefinition liquid,
            int liquidAmount, int ticks) {
        FluidStack in = from.createFluidStack(fromAmount);
        FluidStack outGas = gas.createFluidStack(gasAmount);
        FluidStack outLiquid = liquid.createFluidStack(liquidAmount);
        BuildcraftRecipeRegistry.complexRefinery.addDistilationRecipe(in, outGas, outLiquid, ticks, false);
    }

    private static void addNormalFuel(FluidDefinition def, int amountDiff, double multiplier, double efficiencyMultiplier) {
        final int powerBase = 10;
        final int timeBase = 256_000;

        int powerPerCycle = (int) (multiplier * powerBase);
        int totalTime = (int) (timeBase * efficiencyMultiplier / multiplier / amountDiff);
        BuildcraftFuelRegistry.fuel.addFuel(def.fluid, powerPerCycle, totalTime);
    }

    private static void addDirtyFuel(FluidDefinition def, int amountDiff, double multiplier, double efficiencyMultiplier) {
        final int powerBase = 10;
        final int timeBase = 256_000;

        int powerPerCycle = (int) (multiplier * powerBase);
        int totalTime = (int) (timeBase * efficiencyMultiplier / multiplier / amountDiff);
        BuildcraftFuelRegistry.fuel.addDirtyFuel(def.fluid, powerPerCycle, totalTime, oilResidue[0].createFluidStack(1000 / amountDiff));
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelBakeEvent event) {
        for (FluidDefinition def : allFluids) {
            IModel model = new ModelFluid(def.fluid);
            IBakedModel baked = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
            ModelResourceLocation loc = ModelHelper.getBlockResourceLocation(def.block);
            event.getModelRegistry().putObject(loc, baked);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void textureStitchPre(TextureStitchEvent.Pre event) {
        for (FluidDefinition def : allFluids) {
            int heat = def.fluid.getHeatValue();
            String from = "buildcraftenergy:blocks/fluids/heat_" + heat;
            SpriteColourMapper mapper = new SpriteColourMapper(def.fluid, from + "_still", true);
            event.getMap().setTextureEntry(mapper);

            mapper = new SpriteColourMapper(def.fluid, from + "_flow", false);
            event.getMap().setTextureEntry(mapper);
        }
    }
}
