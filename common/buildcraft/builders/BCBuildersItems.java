/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import java.util.ArrayList;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.IItemBuildCraft;

import buildcraft.builders.item.ItemFillingPlanner;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;

@Mod.EventBusSubscriber(modid = BCBuilders.MODID)
@GameRegistry.ObjectHolder(BCBuilders.MODID)
public class BCBuildersItems {
    @GameRegistry.ObjectHolder("schematic_single")
    public static final ItemSchematicSingle schematicSingle = null;
    public static final ItemSnapshot snapshot = null;
    @GameRegistry.ObjectHolder("filling_planner")
    public static final ItemFillingPlanner fillingPlanner = null;

    private static ArrayList<IItemBuildCraft> items = new ArrayList<>();

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
        schematicSingle.registerVariants();
        snapshot.registerVariants();
        fillingPlanner.registerVariants();
    }
}
