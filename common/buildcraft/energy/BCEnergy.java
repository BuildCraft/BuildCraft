/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.RegistryHelper;

@Mod(modid = BCEnergy.MODID, name = "BuildCraft Energy", dependencies = "required-after:buildcraftcore", version = BCLib.VERSION)
public class BCEnergy {
    public static final String MODID = "buildcraftenergy";

    @Mod.Instance(MODID)
    public static BCEnergy INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);
        BCEnergyItems.preInit();
        BCEnergyBlocks.preInit();
        BCEnergyEntities.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {

    }
}
