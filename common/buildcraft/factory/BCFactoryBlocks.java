/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

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
    public static final BlockAutoWorkbenchItems AUTOWORKBENCH_ITEM = null;
    public static final BlockMiningWell MINING_WELL = null;
    public static final BlockPump PUMP = null;
    public static final BlockTube TUBE = null;
    public static final BlockFloodGate FLOOD_GATE = null;
    public static final BlockTank TANK = null;
    public static final BlockChute CHUTE = null;
    public static final BlockDistiller DISTILLER = null;
    public static final BlockHeatExchange HEAT_EXCHANGE_START = null;
    public static final BlockHeatExchange HEAT_EXCHANGE_MIDDLE = null;
    public static final BlockHeatExchange HEAT_EXCHANGE_END = null;

    // public static Block autoWorkbenchFluids;
    // public static BlockPlastic plastic;
    public static BlockWaterGel WATER_GEL;


    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        RegistryHelper.registerBlocks(event,
            new BlockAutoWorkbenchItems(Material.ROCK, "block.autoworkbench.item"),
            new BlockMiningWell(Material.ROCK, "block.mining_well"),
            new BlockPump(Material.ROCK, "block.pump"),
            new BlockTube(Material.IRON, "block.tube"),
            new BlockFloodGate(Material.ROCK, "block.flood_gate"),
            new BlockTank(Material.ROCK, "block.tank"),
            new BlockChute(Material.ROCK, "block.chute"),
            new BlockDistiller(Material.IRON, "block.distiller"),
            //new BlockPlastic("block.plastic")
            new BlockHeatExchange(Material.IRON, "block.heat_exchange.start", BlockHeatExchange.Part.START),
            new BlockHeatExchange(Material.IRON, "block.heat_exchange.middle", BlockHeatExchange.Part.MIDDLE),
            new BlockHeatExchange(Material.IRON, "block.heat_exchange.end", BlockHeatExchange.Part.END)
            );

        if (BCLib.DEV) {
            RegistryHelper.registerBlocks(event,
                new BlockWaterGel(Material.CLAY, "block.water_gel")
            );
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        RegistryHelper.registerItems(event,
            AUTOWORKBENCH_ITEM,
            MINING_WELL,
            PUMP,
            FLOOD_GATE,
            TANK,
            CHUTE,
            DISTILLER,
            HEAT_EXCHANGE_START,
            HEAT_EXCHANGE_MIDDLE,
            HEAT_EXCHANGE_END
        );
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        RegistryHelper.registerVariants(
            AUTOWORKBENCH_ITEM,
            MINING_WELL,
            PUMP,
            FLOOD_GATE,
            TANK,
            CHUTE,
            DISTILLER,
            HEAT_EXCHANGE_START,
            HEAT_EXCHANGE_MIDDLE,
            HEAT_EXCHANGE_END
        );
    }
}
