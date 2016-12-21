package buildcraft.energy;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.mj.MjAPI;

public class BCEnergyRecipes {
    public static void init() {
        BuildcraftFuelRegistry.fuel.addFuel(BCEnergyFluids.oil, 3 * MjAPI.MJ, 5000);
        // temp
        BuildcraftFuelRegistry.fuel.addFuel(FluidRegistry.LAVA, 6 * MjAPI.MJ, 2000);

        BuildcraftFuelRegistry.coolant.addCoolant(FluidRegistry.WATER, 0.0023f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.ICE), new FluidStack(FluidRegistry.WATER, 1000), 1.5f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.PACKED_ICE), new FluidStack(FluidRegistry.WATER, 1000), 2f);
    }
}
