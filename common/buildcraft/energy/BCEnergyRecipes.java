package buildcraft.energy;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.mj.MjAPI;

import buildcraft.core.BCCoreBlocks;
import buildcraft.lib.recipe.OredictionaryNames;
import buildcraft.lib.recipe.RecipeBuilderShaped;

public class BCEnergyRecipes {
    public static void init() {
        if (BCCoreBlocks.engine != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("mmm");
            builder.add(" g ");
            builder.add("GpG");
            builder.map('g', OredictionaryNames.GLASS_COLOURLESS);
            builder.map('p', Blocks.PISTON);

            if (BCCoreBlocks.engine.isRegistered(EnumEngineType.STONE)) {
                builder.map('m', Blocks.COBBLESTONE);
                builder.map('G', OredictionaryNames.GEAR_STONE);
                builder.setResult(BCCoreBlocks.engine.getStack(EnumEngineType.STONE));
                builder.register();
            }

            if (BCCoreBlocks.engine.isRegistered(EnumEngineType.IRON)) {
                builder.map('m', "ingotIron");
                builder.map('G', OredictionaryNames.GEAR_IRON);
                builder.setResult(BCCoreBlocks.engine.getStack(EnumEngineType.IRON));
                builder.register();
            }
        }

        BuildcraftFuelRegistry.coolant.addCoolant(FluidRegistry.WATER, 0.0023f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.ICE), new FluidStack(FluidRegistry.WATER, 1000), 1.5f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.PACKED_ICE), new FluidStack(FluidRegistry.WATER, 1000), 2f);

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

        addFuel(BCEnergyFluids.fuelGaseous, _gas, 4);
        addFuel(BCEnergyFluids.fuelLight, _light, 6);
        addFuel(BCEnergyFluids.fuelDense, _dense, 8);

        addFuel(BCEnergyFluids.fuelMixedLight, _gas_light, 3);
        addFuel(BCEnergyFluids.fuelMixedHeavy, _light_dense, 5);
        addDirtyFuel(BCEnergyFluids.oilDense, _dense_residue, 4);

        addFuel(BCEnergyFluids.oilDistilled, _gas_light_dense, 1);
        addDirtyFuel(BCEnergyFluids.oilHeavy, _light_dense_residue, 2);

        addDirtyFuel(BCEnergyFluids.crudeOil, _oil, 3);
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
            BuildcraftFuelRegistry.fuel.addDirtyFuel(fuel, powerPerCycle, totalTime, new FluidStack(residue, 1000 / amountDiff));
        }
    }
}
