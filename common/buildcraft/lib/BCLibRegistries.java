package buildcraft.lib;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.crops.CropManager;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;

import buildcraft.lib.crops.CropHandlerPlantable;
import buildcraft.lib.crops.CropHandlerReeds;
import buildcraft.lib.fluid.CoolantRegistry;
import buildcraft.lib.fluid.FuelRegistry;
import buildcraft.lib.misc.FakePlayerUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.lib.recipe.RefineryRecipeRegistry;

public class BCLibRegistries {
    public static void fmlPreInit() {
        BuildcraftRecipeRegistry.assemblyRecipes = AssemblyRecipeRegistry.INSTANCE;
        BuildcraftRecipeRegistry.integrationRecipes = IntegrationRecipeRegistry.INSTANCE;
        BuildcraftRecipeRegistry.refineryRecipes = RefineryRecipeRegistry.INSTANCE;
        BuildcraftFuelRegistry.fuel = FuelRegistry.INSTANCE;
        BuildcraftFuelRegistry.coolant = CoolantRegistry.INSTANCE;
        BuildCraftAPI.fakePlayerProvider = FakePlayerUtil.INSTANCE;

        CropManager.setDefaultHandler(CropHandlerPlantable.INSTANCE);
        CropManager.registerHandler(CropHandlerReeds.INSTANCE);
    }

    public static void fmlInit() {

    }
}
