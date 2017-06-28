/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.api.enums.EnumLaserTableType;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistryHelper;

import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.block.BlockLaserTable;

@Mod.EventBusSubscriber(modid = BCSilicon.MODID)
@GameRegistry.ObjectHolder(BCSilicon.MODID)
public class BCSiliconBlocks {
    public static final BlockLaser LASER = null;
    public static final BlockLaserTable ASSEMBLY_TABLE = null;
    public static final BlockLaserTable ADVANCED_CRAFTING_TABLE = null;
    public static final BlockLaserTable INTEGRATION_TABLE = null;
    public static final BlockLaserTable CHARGING_TABLE = null;
    public static final BlockLaserTable PROGRAMMING_TABLE = null;


    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
            new BlockLaser(Material.ROCK, "block.laser"),
            new BlockLaserTable(EnumLaserTableType.ASSEMBLY_TABLE, Material.ROCK, "block.assembly_table"),
            new BlockLaserTable(EnumLaserTableType.ADVANCED_CRAFTING_TABLE, Material.ROCK, "block.advanced_crafting_table"),
            new BlockLaserTable(EnumLaserTableType.INTEGRATION_TABLE, Material.ROCK, "block.integration_table")
        );

        if (BCLib.DEV) {
            event.getRegistry().registerAll(
                new BlockLaserTable(EnumLaserTableType.CHARGING_TABLE, Material.ROCK, "block.charging_table"),
                new BlockLaserTable(EnumLaserTableType.PROGRAMMING_TABLE, Material.ROCK, "block.programming_table")
            );
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        RegistryHelper.registerItems(event,
            LASER,
            ASSEMBLY_TABLE,
            ADVANCED_CRAFTING_TABLE,
            INTEGRATION_TABLE,
            CHARGING_TABLE,
            PROGRAMMING_TABLE
        );
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        RegistryHelper.registerVariants(
            LASER,
            ASSEMBLY_TABLE,
            ADVANCED_CRAFTING_TABLE,
            INTEGRATION_TABLE,
            CHARGING_TABLE,
            PROGRAMMING_TABLE
        );
    }
}
