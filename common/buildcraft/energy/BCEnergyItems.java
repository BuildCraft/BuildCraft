/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemBC_Neptune;

@Mod.EventBusSubscriber(modid = BCEnergy.MODID)
@GameRegistry.ObjectHolder(BCEnergy.MODID)
public class BCEnergyItems {
    public static final ItemBC_Neptune GLOB_OIL = null;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        if (BCLib.DEV) {
            event.getRegistry().register(new ItemBC_Neptune("item.glob.oil"));
        }
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        if (BCLib.DEV) {
            GLOB_OIL.registerVariants();
        }
    }
}
