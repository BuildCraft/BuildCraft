/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistryHelper;

import buildcraft.builders.block.BlockArchitectTable;
import buildcraft.builders.block.BlockBuilder;
import buildcraft.builders.block.BlockElectronicLibrary;
import buildcraft.builders.block.BlockFiller;
import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.block.BlockQuarry;
import buildcraft.builders.block.BlockReplacer;

@Mod.EventBusSubscriber(modid = BCBuilders.MODID)
@GameRegistry.ObjectHolder(BCBuilders.MODID)
public class BCBuildersBlocks{
    public static final BlockArchitectTable ARCHITECT = null;
    public static final BlockBuilder BUILDER = null;
    public static final BlockFiller FILLER = null;
    public static final BlockElectronicLibrary LIBRARY = null;
    public static final BlockReplacer REPLACER = null;

    public static final BlockFrame FRAME = null;
    public static final BlockQuarry QUARRY = null;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {

        event.getRegistry().registerAll(
            new BlockArchitectTable(Material.IRON, "block.architect"),
            new BlockBuilder(Material.IRON, "block.builder"),
            new BlockFiller(Material.IRON, "block.filler"),
            new BlockElectronicLibrary(Material.IRON, "block.library"),
            new BlockFrame(Material.ROCK, "block.frame"),
            new BlockQuarry(Material.ROCK, "block.quarry")
        );
        if (BCLib.DEV) {
            event.getRegistry().register(new BlockReplacer(Material.IRON, "block.replacer"));
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        RegistryHelper.registerItems(event,
            ARCHITECT,
            BUILDER,
            FILLER,
            LIBRARY,
            REPLACER,
            FRAME,
            QUARRY
        );

    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        RegistryHelper.registerVariants(
            ARCHITECT,
            BUILDER,
            FILLER,
            LIBRARY,
            REPLACER,
            FRAME,
            QUARRY);
    }


}
