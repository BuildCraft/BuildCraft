/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.crops.CropManager;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.registry.BuildCraftRegistryManager;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.lib.crops.CropHandlerPlantable;
import buildcraft.lib.crops.CropHandlerReeds;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.misc.FakePlayerProvider;
import buildcraft.lib.recipe.coolant.CoolantRegistry;
import buildcraft.lib.recipe.fuel.FuelRegistry;
import buildcraft.lib.recipe.integration.IntegrationRecipeRegistry;
import buildcraft.lib.recipe.refinery.RefineryRecipeRegistry;
import buildcraft.lib.registry.PluggableRegistry;
import buildcraft.lib.script.ReloadableRegistryManager;

public class BCLibRegistries {
    public static void fmlPreInit() {
//        BuildcraftRecipeRegistry.integrationRecipes = IntegrationRecipeRegistry.INSTANCE;
//        BuildcraftRecipeRegistry.refineryRecipes = RefineryRecipeRegistry.INSTANCE;
//        BuildcraftFuelRegistry.fuel = FuelRegistry.INSTANCE;
//        BuildcraftFuelRegistry.coolant = CoolantRegistry.INSTANCE;
        BuildCraftAPI.fakePlayerProvider = FakePlayerProvider.INSTANCE;
        PipeApi.pluggableRegistry = PluggableRegistry.INSTANCE;

        ReloadableRegistryManager dataManager = ReloadableRegistryManager.DATA_PACKS;
        BuildCraftRegistryManager.managerDataPacks = dataManager;
        dataManager.registerRegistry(GuideBookRegistry.INSTANCE);

        CropManager.setDefaultHandler(CropHandlerPlantable.INSTANCE);
        CropManager.registerHandler(CropHandlerReeds.INSTANCE);
    }

    public static void initRecipeRegistry() {
        BuildcraftRecipeRegistry.integrationRecipes = IntegrationRecipeRegistry.INSTANCE;
        BuildcraftRecipeRegistry.refineryRecipes = RefineryRecipeRegistry.INSTANCE;
        BuildcraftFuelRegistry.fuel = FuelRegistry.INSTANCE;
        BuildcraftFuelRegistry.coolant = CoolantRegistry.INSTANCE;
    }

    public static void fmlInit() {
    }
}
