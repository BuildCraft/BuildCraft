/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.BCModules;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager.IDistillationRecipe;

import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.misc.MathUtil;

public class BCEnergyRecipes {
    public static void init() {

        BuildcraftFuelRegistry.coolant.addCoolant(FluidRegistry.WATER, 0.0023f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.ICE),
            new FluidStack(FluidRegistry.WATER, 1000), 1.5f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.PACKED_ICE),
            new FluidStack(FluidRegistry.WATER, 1000), 2f);

        // Relative amounts of the fluid -- the amount of oil used in refining will return X amount of fluid

        // single
        final int _oil = 8;
        final int _gas = 16;
        final int _light = 4;
        final int _dense = 2;
        final int _residue = 1;

        // double
        final int _gas_light = 10;
        final int _light_dense = 5;
        final int _dense_residue = 2;

        // triple
        final int _light_dense_residue = 3;
        final int _gas_light_dense = 8;

        addFuel(BCEnergyFluids.fuelGaseous, _gas, 8);
        addFuel(BCEnergyFluids.fuelLight, _light, 6);
        addFuel(BCEnergyFluids.fuelDense, _dense, 4);

        addFuel(BCEnergyFluids.fuelMixedLight, _gas_light, 3);
        addFuel(BCEnergyFluids.fuelMixedHeavy, _light_dense, 5);
        addDirtyFuel(BCEnergyFluids.oilDense, _dense_residue, 4);

        addFuel(BCEnergyFluids.oilDistilled, _gas_light_dense, 1);
        addDirtyFuel(BCEnergyFluids.oilHeavy, _light_dense_residue, 2);

        addDirtyFuel(BCEnergyFluids.crudeOil, _oil, 3);

        if (BCModules.FACTORY.isLoaded()) {
            FluidStack[] gas_light_dense_residue = createFluidStack(BCEnergyFluids.crudeOil, _oil);
            FluidStack[] gas_light_dense = createFluidStack(BCEnergyFluids.oilDistilled, _gas_light_dense);
            FluidStack[] gas_light = createFluidStack(BCEnergyFluids.fuelMixedLight, _gas_light);
            FluidStack[] gas = createFluidStack(BCEnergyFluids.fuelGaseous, _gas);
            FluidStack[] light_dense_residue = createFluidStack(BCEnergyFluids.oilHeavy, _light_dense_residue);
            FluidStack[] light_dense = createFluidStack(BCEnergyFluids.fuelMixedHeavy, _light_dense);
            FluidStack[] light = createFluidStack(BCEnergyFluids.fuelLight, _light);
            FluidStack[] dense_residue = createFluidStack(BCEnergyFluids.oilDense, _dense_residue);
            FluidStack[] dense = createFluidStack(BCEnergyFluids.fuelDense, _dense);
            FluidStack[] residue = createFluidStack(BCEnergyFluids.oilResidue, _residue);

            addDistillation(gas_light_dense_residue, gas, light_dense_residue, 0, 32 * MjAPI.MJ);
            addDistillation(gas_light_dense_residue, gas_light, dense_residue, 1, 16 * MjAPI.MJ);
            addDistillation(gas_light_dense_residue, gas_light_dense, residue, 2, 12 * MjAPI.MJ);

            addDistillation(gas_light_dense, gas, light_dense, 0, 24 * MjAPI.MJ);
            addDistillation(gas_light_dense, gas_light, dense, 1, 16 * MjAPI.MJ);

            addDistillation(gas_light, gas, light, 0, 24 * MjAPI.MJ);

            addDistillation(light_dense_residue, light, dense_residue, 1, 16 * MjAPI.MJ);
            addDistillation(light_dense_residue, light_dense, residue, 2, 12 * MjAPI.MJ);

            addDistillation(light_dense, light, dense, 1, 16 * MjAPI.MJ);

            addDistillation(dense_residue, dense, residue, 2, 12 * MjAPI.MJ);

            addHeatExchange(BCEnergyFluids.crudeOil);
            addHeatExchange(BCEnergyFluids.oilDistilled);
            addHeatExchange(BCEnergyFluids.oilHeavy);
            addHeatExchange(BCEnergyFluids.fuelMixedLight);
            addHeatExchange(BCEnergyFluids.fuelMixedHeavy);
            addHeatExchange(BCEnergyFluids.oilDense);
            addHeatExchange(BCEnergyFluids.fuelGaseous);
            addHeatExchange(BCEnergyFluids.fuelLight);
            addHeatExchange(BCEnergyFluids.fuelDense);
            addHeatExchange(BCEnergyFluids.oilResidue);

            FluidStack water = new FluidStack(FluidRegistry.WATER, 10);
            BuildcraftRecipeRegistry.refineryRecipes.addHeatableRecipe(water, null, 0, 1);

            FluidStack lava = new FluidStack(FluidRegistry.LAVA, 5);
            BuildcraftRecipeRegistry.refineryRecipes.addCoolableRecipe(lava, null, 4, 2);
        }
    }

    private static FluidStack[] createFluidStack(Fluid[] fluid, int amount) {
        FluidStack[] arr = new FluidStack[fluid.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new FluidStack(fluid[i], amount);
        }
        return arr;
    }

    private static Fluid getFirstOrNull(Fluid[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[0];
    }

    private static final int TIME_BASE = 240_000; // 240_000 - multiple of 3, 5, 16, 1000

    private static void addFuel(Fluid[] in, int amountDiff, int multiplier) {
        Fluid fuel = getFirstOrNull(in);
        if (fuel == null) {// It may have been disabled
            return;
        }
        long powerPerCycle = multiplier * MjAPI.MJ;
        int totalTime = TIME_BASE / multiplier / amountDiff;
        BuildcraftFuelRegistry.fuel.addFuel(fuel, powerPerCycle, totalTime);
    }

    private static void addDirtyFuel(Fluid[] in, int amountDiff, int multiplier) {
        Fluid fuel = getFirstOrNull(in);
        if (fuel == null) {// It may have been disabled
            return;
        }
        long powerPerCycle = multiplier * MjAPI.MJ;
        int totalTime = TIME_BASE / multiplier / amountDiff;
        Fluid residue = getFirstOrNull(BCEnergyFluids.oilResidue);
        if (residue == null) {// residue might have been disabled
            BuildcraftFuelRegistry.fuel.addFuel(fuel, powerPerCycle, totalTime);
        } else {
            BuildcraftFuelRegistry.fuel.addDirtyFuel(fuel, powerPerCycle, totalTime,
                new FluidStack(residue, 1000 / amountDiff));
        }
    }

    private static void addDistillation(FluidStack[] in, FluidStack[] outGas, FluidStack[] outLiquid, int heat,
        long mjCost) {
        FluidStack _in = in[heat];
        FluidStack _outGas = outGas[heat];
        FluidStack _outLiquid = outLiquid[heat];
        IDistillationRecipe existing =
            BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getRecipeForInput(_in);
        if (existing != null) {
            throw new IllegalStateException("Already added distillation recipe for " + _in.getFluid().getName());
        }
        int hcf = MathUtil.findHighestCommonFactor(_in.amount, _outGas.amount);
        hcf = MathUtil.findHighestCommonFactor(hcf, _outLiquid.amount);
        if (hcf > 1) {
            (_in = _in.copy()).amount /= hcf;
            (_outGas = _outGas.copy()).amount /= hcf;
            (_outLiquid = _outLiquid.copy()).amount /= hcf;
            mjCost /= hcf;
        }
        BuildcraftRecipeRegistry.refineryRecipes.addDistillationRecipe(_in, _outGas, _outLiquid, mjCost);
    }

    private static void addHeatExchange(BCFluid[] fluid) {
        for (int i = 0; i < fluid.length - 1; i++) {
            BCFluid cool = fluid[i];
            BCFluid hot = fluid[i + 1];
            FluidStack cool_f = new FluidStack(cool, 10);
            FluidStack hot_f = new FluidStack(hot, 10);
            int ch = cool.getHeatValue();
            int hh = hot.getHeatValue();
            BuildcraftRecipeRegistry.refineryRecipes.addHeatableRecipe(cool_f, hot_f, ch, hh);
            BuildcraftRecipeRegistry.refineryRecipes.addCoolableRecipe(hot_f, cool_f, hh, ch);
        }
    }
}
