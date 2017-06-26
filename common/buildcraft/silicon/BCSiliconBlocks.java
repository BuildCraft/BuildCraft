/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import java.util.ArrayList;

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
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.registry.RegistryHelper;

import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.block.BlockLaserTable;

@Mod.EventBusSubscriber(modid = BCSilicon.MODID)
@GameRegistry.ObjectHolder(BCSilicon.MODID)
public class BCSiliconBlocks {
    public static final BlockLaser laser = null;
    @GameRegistry.ObjectHolder("assembly_table")
    public static final BlockLaserTable assemblyTable = null;
    @GameRegistry.ObjectHolder("advanced_crafting_table")
    public static final BlockLaserTable advancedCraftingTable = null;
    @GameRegistry.ObjectHolder("integration_table")
    public static final BlockLaserTable integrationTable = null;
    @GameRegistry.ObjectHolder("charging_table")
    public static final BlockLaserTable chargingTable = null;
    @GameRegistry.ObjectHolder("programming_table")
    public static final BlockLaserTable programmingTable = null;

    private static ArrayList<IItemBuildCraft> items = new ArrayList<>();

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
        RegistryHelper.listAndRegister(event, items,
            laser,
            assemblyTable,
            advancedCraftingTable,
            integrationTable,
            chargingTable,
            programmingTable
        );
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        items.forEach(IItemBuildCraft::registerVariants);
    }
}
