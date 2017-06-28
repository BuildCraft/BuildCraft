/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.item.ItemBlockBC_Neptune;
import buildcraft.lib.registry.RegistryHelper;

import buildcraft.robotics.block.BlockZonePlanner;

@Mod.EventBusSubscriber(modid = BCRobotics.MODID)
@GameRegistry.ObjectHolder(BCRobotics.MODID)
public class BCRoboticsBlocks {
    public static final BlockZonePlanner ZONE_PLANNER = null;


    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockZonePlanner(Material.ROCK, "block.zone_planner"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        RegistryHelper.registerItems(event, new ItemBlockBC_Neptune(ZONE_PLANNER));
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        RegistryHelper.registerVariants(ZONE_PLANNER);
    }
}
