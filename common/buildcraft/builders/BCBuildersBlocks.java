/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.item.ItemBlockBC_Neptune;
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
    public static final BlockArchitectTable architect = null;
    public static final BlockBuilder builder = null;
    public static final BlockFiller filler = null;
    public static final BlockElectronicLibrary library = null;
    public static final BlockReplacer replacer = null;

    public static final BlockFrame frame = null;
    public static final BlockQuarry quarry = null;

    private static ArrayList<ItemBlockBC_Neptune> items = new ArrayList<>();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {

        event.getRegistry().registerAll(
            new BlockArchitectTable(Material.IRON, "block.architect"),
            new BlockBuilder(Material.IRON, "block.builder"),
            new BlockFiller(Material.IRON, "block.filler"),
            new BlockElectronicLibrary(Material.IRON, "block.library"),
            new BlockReplacer(Material.IRON, "block.replacer"),
            new BlockFrame(Material.ROCK, "block.frame"),
            new BlockQuarry(Material.ROCK, "block.quarry")
        );


    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        RegistryHelper.listAndRegister(event, items,
            architect,
            builder,
            filler,
            library,
            replacer,
            frame,
            quarry
        );

    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        items.forEach(IItemBuildCraft::registerVariants);
    }


}
