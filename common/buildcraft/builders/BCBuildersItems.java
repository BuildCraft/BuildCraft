/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistryHelper;

import buildcraft.builders.item.ItemFillingPlanner;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;

@Mod.EventBusSubscriber(modid = BCBuilders.MODID)
@GameRegistry.ObjectHolder(BCBuilders.MODID)
public class BCBuildersItems {
    public static final ItemSchematicSingle SCHEMATIC_SINGLE = null;
    public static final ItemSnapshot SNAPSHOT = null;
    public static final ItemFillingPlanner FILLING_PLANNER = null;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
            new ItemSnapshot("item.snapshot"),
            new ItemFillingPlanner("item.filling_planner")
        );
        if (BCLib.DEV) {
            event.getRegistry().register(new ItemSchematicSingle("item.schematic.single"));
        }
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        RegistryHelper.registerVariants(
            SCHEMATIC_SINGLE,
            SNAPSHOT,
            FILLING_PLANNER
        );
    }
}
