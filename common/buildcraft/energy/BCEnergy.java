/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.core.BCCore;
import buildcraft.energy.fluid.FluidOil;
import buildcraft.energy.generation.BiomeInitializer;
import buildcraft.energy.generation.BiomeOilDesert;
import buildcraft.energy.generation.BiomeOilOcean;
import buildcraft.energy.generation.OilPopulate;
import buildcraft.lib.BCLib;
import buildcraft.lib.RegistryHelper;
import buildcraft.lib.fluid.FluidManager;

@Mod(modid = BCEnergy.MODID, name = "BuildCraft Energy", dependencies = "required-after:buildcraftcore", version = BCLib.VERSION)
public class BCEnergy {
    public static final String MODID = "buildcraftenergy";
    static {
        FluidRegistry.enableUniversalBucket(); // FIXME: not working
    }

    @Mod.Instance(MODID)
    public static BCEnergy INSTANCE;

    public static FluidOil oil;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);
        BCEnergyItems.preInit();
        BCEnergyFluids.preInit();
        BCEnergyBlocks.preInit();
        BCEnergyEntities.preInit();

        oil = FluidManager.register(new FluidOil("oil", new ResourceLocation("buildcraftenergy", "blocks/fluids/oil_still"), new ResourceLocation("buildcraftenergy", "blocks/fluids/oil_flow")));
        FluidManager.fmlPreInitClient(); // TODO: move from here
        GameRegistry.register(BiomeOilOcean.INSTANCE);
        GameRegistry.register(BiomeOilDesert.INSTANCE);
        MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeInitializer());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(OilPopulate.INSTANCE);
    }
}
