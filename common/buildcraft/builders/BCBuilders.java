/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.api.schematic.GlobalSavedDataSnapshots;
import buildcraft.builders.schematic.RulesLoader;
import buildcraft.builders.schematic.SchematicsLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.builders.bpt.PerSaveBptStorage;
import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistryHelper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = BCBuilders.MODID, name = "BuildCraft Builders", dependencies = "required-after:buildcraftcore", version = BCLib.VERSION)
public class BCBuilders {
    public static final String MODID = "buildcraftbuilders";

    @Mod.Instance(MODID)
    public static BCBuilders INSTANCE = null;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);

        BCBuildersItems.preInit();
        BCBuildersBlocks.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCBuildersProxy.getProxy());
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCBuildersProxy.getProxy().fmlInit();
        BCBuildersRecipes.init();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        RulesLoader.INSTANCE.loadAll();
        SchematicsLoader.INSTANCE.loadAll();
    }

    @Mod.EventHandler
    public static void onServerStarting(FMLServerStartingEvent event) {
        PerSaveBptStorage.onServerStart(event);
        GlobalSavedDataSnapshots.get(Side.SERVER);
        GlobalSavedDataSnapshots.get(Side.CLIENT); // FIXME
    }

    @Mod.EventHandler
    public static void onServerStopping(FMLServerStoppingEvent event) {
        PerSaveBptStorage.onServerStopping();
    }
}
