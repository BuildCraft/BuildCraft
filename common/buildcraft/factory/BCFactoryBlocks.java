/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.registry.RegistryHelper;

import buildcraft.factory.block.BlockAutoWorkbenchItems;
import buildcraft.factory.block.BlockChute;
import buildcraft.factory.block.BlockDistiller;
import buildcraft.factory.block.BlockFloodGate;
import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.block.BlockMiningWell;
import buildcraft.factory.block.BlockPump;
import buildcraft.factory.block.BlockTank;
import buildcraft.factory.block.BlockTube;
import buildcraft.factory.block.BlockWaterGel;

@Mod.EventBusSubscriber(modid = BCFactory.MODID)
@GameRegistry.ObjectHolder(BCFactory.MODID)
public class BCFactoryBlocks {
    @GameRegistry.ObjectHolder("autoworkbench_item")
    public static final BlockAutoWorkbenchItems autoWorkbenchItems = null;
    @GameRegistry.ObjectHolder("mining_well")
    public static final BlockMiningWell miningWell = null;
    public static final BlockPump pump = null;
    public static final BlockTube tube = null;
    @GameRegistry.ObjectHolder("flood_gate")
    public static final BlockFloodGate floodGate = null;
    public static final BlockTank tank = null;
    public static final BlockChute chute = null;
    public static final BlockDistiller distiller = null;
    @GameRegistry.ObjectHolder("heat_exchange_start")
    public static final BlockHeatExchange heatExchangeStart = null;
    @GameRegistry.ObjectHolder("heat_exchange_middle")
    public static final BlockHeatExchange heatExchangeMiddle = null;
    @GameRegistry.ObjectHolder("heat_exchange_end")
    public static final BlockHeatExchange heatExchangeEnd = null;

    // public static Block autoWorkbenchFluids;
    // public static BlockPlastic plastic;
    public static BlockWaterGel waterGel;

    private static ArrayList<IItemBuildCraft> items = new ArrayList<>();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
            new BlockAutoWorkbenchItems(Material.ROCK, "block.autoworkbench.item"),
            new BlockMiningWell(Material.ROCK, "block.mining_well"),
            new BlockPump(Material.ROCK, "block.pump"),
            new BlockTube(Material.IRON, "block.tube"),
            new BlockFloodGate(Material.ROCK, "block.flood_gate"),
            new BlockTank(Material.ROCK, "block.tank"),
            new BlockChute(Material.ROCK, "block.chute"),
            new BlockDistiller(Material.IRON, "block.distiller")
            //new BlockPlastic("block.plastic")
        );

        if (BCLib.DEV) {
            event.getRegistry().registerAll(
                new BlockHeatExchange(Material.IRON, "block.heat_exchange.start", BlockHeatExchange.Part.START),
                new BlockHeatExchange(Material.IRON, "block.heat_exchange.middle", BlockHeatExchange.Part.MIDDLE),
                new BlockHeatExchange(Material.IRON, "block.heat_exchange.end", BlockHeatExchange.Part.END),
                new BlockWaterGel(Material.CLAY, "block.water_gel")

            );
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        RegistryHelper.listAndRegister(event, items,
            autoWorkbenchItems,
            miningWell,
            pump,
            floodGate,
            tank,
            chute,
            distiller,
            heatExchangeStart,
            heatExchangeMiddle,
            heatExchangeEnd
        );
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        items.forEach(IItemBuildCraft::registerVariants);
    }
}
