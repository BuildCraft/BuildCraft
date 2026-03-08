package buildcraft.datagen.energy;

import buildcraft.api.mj.MjAPI;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.fluid.BCFluidAttributes;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.coolant.CoolantRecipeBuilder;
import buildcraft.lib.recipe.fuel.FuelRecipeBuilder;
import buildcraft.lib.recipe.refinery.DistillationRecipeBuilder;
import buildcraft.lib.recipe.refinery.HeatExchangeRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class EnergyOilRecipeGenerator extends RecipeProvider {
    private final ExistingFileHelper existingFileHelper;

    private Consumer<FinishedRecipe> consumer;

    // Relative amounts of the fluid -- the amount of oil used in refining will return X amount of fluid

    // single
    private static final int _oil = 8;
    private static final int _gas = 16;
    private static final int _light = 4;
    private static final int _dense = 2;
    private static final int _residue = 1;

    // double
    private static final int _gas_light = 10;
    private static final int _light_dense = 5;
    private static final int _dense_residue = 2;

    // triple
    private static final int _light_dense_residue = 3;
    private static final int _gas_light_dense = 8;

    private static final int TIME_BASE = 240_000; // 240_000 - multiple of 3, 5, 16, 1000

    public EnergyOilRecipeGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator);
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        this.consumer = consumer;
        buildHeatExchangeRecipes();
        buildDistillationRecipes();
        buildFuelRecipes();
        buildCoolantRecipes();
    }

    private void buildCoolantRecipes() {
//        BuildcraftFuelRegistry.coolant.addCoolant(Fluids.WATER, 0.0023f);
        CoolantRecipeBuilder.fluidCoolant(new FluidStack(Fluids.WATER, 1), 0.0023f).save(consumer, Fluids.WATER.getRegistryName().getPath());
//        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.ICE), new FluidStack(Fluids.WATER, 1000), 1.5f);
        CoolantRecipeBuilder.solidCoolant(new ItemStack(Blocks.ICE), new FluidStack(Fluids.WATER, 1000), 1.5f).save(consumer, Blocks.ICE.getRegistryName().getPath());
//        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.PACKED_ICE), new FluidStack(Fluids.WATER, 1000), 2f);
        CoolantRecipeBuilder.solidCoolant(new ItemStack(Blocks.PACKED_ICE), new FluidStack(Fluids.WATER, 1000), 2f).save(consumer, Blocks.PACKED_ICE.getRegistryName().getPath());
    }

    private void buildHeatExchangeRecipes() {
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

        FluidStack EMPTY = StackUtil.EMPTY_FLUID;
        FluidStack water = new FluidStack(Fluids.WATER, 10);
//        BuildcraftRecipeRegistry.refineryRecipes.addHeatableRecipe(water, EMPTY, 0, 1);
        HeatExchangeRecipeBuilder.heatable(water, EMPTY, 0, 1).save(consumer, Fluids.WATER.getRegistryName().getPath() + "__to__" + Fluids.EMPTY.getRegistryName().getPath());

        FluidStack lava = new FluidStack(Fluids.LAVA, 5);
//        BuildcraftRecipeRegistry.refineryRecipes.addCoolableRecipe(lava, EMPTY, 4, 2);
        HeatExchangeRecipeBuilder.coolable(lava, EMPTY, 4, 2).save(consumer, Fluids.LAVA.getRegistryName().getPath() + "__to__" + Fluids.EMPTY.getRegistryName().getPath());
    }

    private void buildDistillationRecipes() {
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

    }

    private void buildFuelRecipes() {
        addFuel(BCEnergyFluids.fuelGaseous, _gas, 8, 4);
        addFuel(BCEnergyFluids.fuelLight, _light, 6, 6);
        addFuel(BCEnergyFluids.fuelDense, _dense, 4, 12);

        addFuel(BCEnergyFluids.fuelMixedLight, _gas_light, 3, 5);
        addFuel(BCEnergyFluids.fuelMixedHeavy, _light_dense, 5, 8);
        addDirtyFuel(BCEnergyFluids.oilDense, _dense_residue, 4, 4);

        addFuel(BCEnergyFluids.oilDistilled, _gas_light_dense, 1, 5);
        addDirtyFuel(BCEnergyFluids.oilHeavy, _light_dense_residue, 2, 4);

        addDirtyFuel(BCEnergyFluids.crudeOil, _oil, 3, 4);
    }

    private void addHeatExchange(RegistryObject<BCFluid.Source>[] fluid) {
        for (int i = 0; i < fluid.length - 1; i++) {
            BCFluid.Source cool = fluid[i].get();
            BCFluid.Source hot = fluid[i + 1].get();
            FluidStack cool_f = new FluidStack(cool, 10);
            FluidStack hot_f = new FluidStack(hot, 10);
            int ch = ((BCFluidAttributes) cool.getAttributes()).getHeat();
            int hh = ((BCFluidAttributes) hot.getAttributes()).getHeat();
//            BuildcraftRecipeRegistry.refineryRecipes.addHeatableRecipe(cool_f, hot_f, ch, hh);
            HeatExchangeRecipeBuilder.heatable(cool_f, hot_f, ch, hh).save(consumer, cool.getRegistryName().getPath() + "__to__" + hot.getRegistryName().getPath());
//            BuildcraftRecipeRegistry.refineryRecipes.addCoolableRecipe(hot_f, cool_f, hh, ch);
            HeatExchangeRecipeBuilder.coolable(hot_f, cool_f, hh, ch).save(consumer, hot.getRegistryName().getPath() + "__to__" + cool.getRegistryName().getPath());
        }
    }

    private void addDistillation(FluidStack[] in, FluidStack[] outGas, FluidStack[] outLiquid, int heat, long mjCost) {
        FluidStack _in = in[heat];
        FluidStack _outGas = outGas[heat];
        FluidStack _outLiquid = outLiquid[heat];
        int hcf = MathUtil.findHighestCommonFactor(_in.getAmount(), _outGas.getAmount());
        hcf = MathUtil.findHighestCommonFactor(hcf, _outLiquid.getAmount());
        if (hcf > 1) {
//            (_in = _in.copy()).amount /= hcf;
            _in = _in.copy();
            _in.setAmount(_in.getAmount() / hcf);
//            (_outGas = _outGas.copy()).amount /= hcf;
            _outGas = _outGas.copy();
            _outGas.setAmount(_outGas.getAmount() / hcf);
//            (_outLiquid = _outLiquid.copy()).amount /= hcf;
            _outLiquid = _outLiquid.copy();
            _outLiquid.setAmount(_outLiquid.getAmount() / hcf);
            mjCost /= hcf;
        }
//        BuildcraftRecipeRegistry.refineryRecipes.addDistillationRecipe(_in, _outGas, _outLiquid, mjCost);
        DistillationRecipeBuilder.distillation(mjCost, _in, _outGas, _outLiquid).save(consumer, _in.getFluid().getRegistryName().getPath());
    }

    private static FluidStack[] createFluidStack(RegistryObject<? extends Fluid>[] fluid, int amount) {
        FluidStack[] arr = new FluidStack[fluid.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new FluidStack(fluid[i].get(), amount);
        }
        return arr;
    }

    private void addFuel(RegistryObject<? extends Fluid>[] in, int amountDiff, int multiplier, int boostOver4) {
        Fluid fuel = getFirstOrNull(in);
        if (fuel == null) {// It may have been disabled
            return;
        }
        long powerPerCycle = multiplier * MjAPI.MJ;
        int totalTime = TIME_BASE * boostOver4 / 4 / multiplier / amountDiff;
//        BuildcraftFuelRegistry.fuel.addFuel(fuel, powerPerCycle, totalTime);
        FuelRecipeBuilder.fuel(new FluidStack(fuel, 1), powerPerCycle, totalTime).save(consumer, fuel.getRegistryName().getPath());
    }

    private void addDirtyFuel(RegistryObject<? extends Fluid>[] in, int amountDiff, int multiplier, int boostOver4) {
        Fluid fuel = getFirstOrNull(in);
        if (fuel == null) {// It may have been disabled
            return;
        }
        long powerPerCycle = multiplier * MjAPI.MJ;
        int totalTime = TIME_BASE * boostOver4 / 4 / multiplier / amountDiff;
        Fluid residue = getFirstOrNull(BCEnergyFluids.oilResidue);
        if (residue == null) {// residue might have been disabled
//            BuildcraftFuelRegistry.fuel.addFuel(fuel, powerPerCycle, totalTime);
            FuelRecipeBuilder.fuel(new FluidStack(fuel, 1), powerPerCycle, totalTime).save(consumer, fuel.getRegistryName().getPath());
        } else {
//            BuildcraftFuelRegistry.fuel.addDirtyFuel(fuel, powerPerCycle, totalTime, new FluidStack(residue, 1000 / amountDiff));
            FuelRecipeBuilder.dirtyFuel(new FluidStack(fuel, 1), powerPerCycle, totalTime, new FluidStack(residue, 1000 / amountDiff)).save(consumer, fuel.getRegistryName().getPath());
        }
    }

    private static Fluid getFirstOrNull(RegistryObject<? extends Fluid>[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[0].get();
    }

    @Override
    public String getName() {
        return "BuildCraft Energy Oil Recipe Generator";
    }
}
