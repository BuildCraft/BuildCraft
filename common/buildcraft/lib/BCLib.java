/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.api.BCModules;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;

import buildcraft.lib.block.VanillaPaintHandlers;
import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.bpt.vanilla.VanillaSchematics;
import buildcraft.lib.item.ItemManager;
import buildcraft.lib.list.VanillaListHandlers;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.misc.FakePlayerUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;

//@formatter:off
@Mod(modid = BCLib.MODID,
     name = "BuildCraft Lib",
     version = BCLib.VERSION,
     acceptedMinecraftVersions = "[1.10.2]",
     dependencies = "required-after:Forge@[12.17.0.1909,)")
//@formatter:on
public class BCLib {
    public static final String MODID = "buildcraftlib";
    public static final String VERSION = "@VERSION@";

    @Instance(MODID)
    public static BCLib INSTANCE;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        BuildcraftRecipeRegistry.assemblyRecipes = AssemblyRecipeRegistry.INSTANCE;
        BuildcraftRecipeRegistry.integrationRecipes = IntegrationRecipeRegistry.INSTANCE;

        BCModules.fmlPreInit();
        BuildCraftAPI.fakePlayerProvider = FakePlayerUtil.INSTANCE;
        BCLibProxy.getProxy().fmlPreInit();

        BCLibItems.preInit();

        BCMessageHandler.fmlPreInit();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCLibProxy.getProxy());

        MinecraftForge.EVENT_BUS.register(BCLibEventDist.INSTANCE);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCLibProxy.getProxy().fmlInit();

        VanillaSchematics.fmlInit();
        VanillaListHandlers.fmlInit();
        VanillaPaintHandlers.fmlInit();
        VanillaRotationHandlers.fmlInit();

        ItemManager.fmlInit();

        BCLibRecipes.fmlInit();

        BCLibDatabase.fmlInit();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        BCLibProxy.getProxy().fmlPostInit();
        BCMessageHandler.fmlPostInit();
        VanillaListHandlers.fmlPostInit();
        MarkerCache.postInit();
    }

    @Mod.EventHandler
    public static void onServerStarted(FMLServerStartedEvent evt) {
        BCLibEventDist.onServerStarted(evt);
    }
}
