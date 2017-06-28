/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.registry.RegistryHelper;

import buildcraft.transport.block.BlockFilteredBuffer;
import buildcraft.transport.block.BlockPipeHolder;

@Mod.EventBusSubscriber(modid = BCTransport.MODID)
@GameRegistry.ObjectHolder(BCTransport.MODID)
public class BCTransportBlocks {
    public static final BlockFilteredBuffer FILTERED_BUFFER = null;
    public static final BlockPipeHolder PIPE_HOLDER = null;


    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
            new BlockFilteredBuffer(Material.ROCK, "block.filtered_buffer"),
            new BlockPipeHolder(Material.IRON, "block.pipe_holder")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        RegistryHelper.registerItems(event, FILTERED_BUFFER);
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        RegistryHelper.registerVariants(FILTERED_BUFFER);
    }
}
