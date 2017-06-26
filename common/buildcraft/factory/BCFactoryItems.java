/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemBC_Neptune;

import buildcraft.factory.item.ItemWaterGel;
@Mod.EventBusSubscriber(modid = BCFactory.MODID)
@GameRegistry.ObjectHolder(BCFactory.MODID)

public class BCFactoryItems {
    @GameRegistry.ObjectHolder("plastic_sheet")
    public static final ItemBC_Neptune plasticSheet = null;
    @GameRegistry.ObjectHolder("water_gel_spawn")
    public static final ItemWaterGel waterGel = null;
    @GameRegistry.ObjectHolder("gel")
    public static final ItemBC_Neptune gelledWater = null;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        if (BCLib.DEV) {
            event.getRegistry().register(new ItemBC_Neptune("item.plastic.sheet"));
        }
        event.getRegistry().registerAll(
            new ItemWaterGel("item.water_gel_spawn"),
            new ItemBC_Neptune("item.gel")
        );
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        if (BCLib.DEV) {
            plasticSheet.registerVariants();
        }
        waterGel.registerVariants();
        gelledWater.registerVariants();
    }
}
