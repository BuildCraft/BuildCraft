/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.RegistryHelper;

@Mod(modid = BCBuilders.MODID, name = "BuildCraft Builders", dependencies = "required-after:buildcraftcore", version = BCLib.VERSION)
public class BCBuilders {
    public static final String MODID = "buildcraftbuilders";

    @Mod.Instance(MODID)
    public static BCBuilders INSTANCE = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);

        BCBuildersItems.preInit();
        BCBuildersBlocks.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BuildersProxy_Neptune.getProxy());

        MinecraftForge.EVENT_BUS.register(BCBuildersEventDist.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        BuildersProxy_Neptune.getProxy().fmlInit();
        BCBuildersRecipes.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {

    }
}
