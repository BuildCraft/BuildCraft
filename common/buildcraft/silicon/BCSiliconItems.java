/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.registry.RegistryHelper;

import buildcraft.silicon.item.ItemRedstoneChipset;

@Mod.EventBusSubscriber(modid = BCSilicon.MODID)
@GameRegistry.ObjectHolder(BCSilicon.MODID)
public class BCSiliconItems {
    public static final ItemRedstoneChipset REDSTONE_CHIPSET = null;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemRedstoneChipset("item.redstone_chipset"));
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        RegistryHelper.registerVariants(REDSTONE_CHIPSET);
    }
}
