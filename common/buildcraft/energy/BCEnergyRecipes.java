package buildcraft.energy;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

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

        BuildcraftFuelRegistry.fuel.addFuel(BCEnergyFluids.crudeOil[0], 3 * MjAPI.MJ, 5000);

        BuildcraftFuelRegistry.coolant.addCoolant(FluidRegistry.WATER, 0.0023f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.ICE), new FluidStack(FluidRegistry.WATER, 1000), 1.5f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.PACKED_ICE), new FluidStack(FluidRegistry.WATER, 1000), 2f);
    }
}
