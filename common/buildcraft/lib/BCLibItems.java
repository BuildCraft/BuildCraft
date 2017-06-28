/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.item.ItemDebugger;
import buildcraft.lib.item.ItemGuide;
import buildcraft.lib.registry.RegistryHelper;

@Mod.EventBusSubscriber(modid = BCLib.MODID)
@GameRegistry.ObjectHolder(BCLib.MODID)
public class BCLibItems {
    public static final ItemGuide GUIDE = null;
    public static final ItemDebugger DEBUGGER = null;

    private static boolean enableGuide, enableDebugger;

    public static void enableGuide() {
        enableGuide = true;
    }

    public static void enableDebugger() {
        enableDebugger = true;
    }

    public static boolean isGuideEnabled() {
        return enableGuide;
    }

    public static boolean isDebuggerEnabled() {
        return enableDebugger;
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
            new ItemGuide("item.guide"),
            new ItemDebugger("item.debugger")
        );
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        RegistryHelper.registerVariants(
            GUIDE,
            DEBUGGER
        );
    }
}
