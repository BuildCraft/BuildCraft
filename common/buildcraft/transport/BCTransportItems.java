/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.registry.RegistryHelper;

import buildcraft.transport.item.ItemPipeHolder;
import buildcraft.transport.item.ItemPluggableFacade;
import buildcraft.transport.item.ItemPluggableGate;
import buildcraft.transport.item.ItemPluggableLens;
import buildcraft.transport.item.ItemPluggablePulsar;
import buildcraft.transport.item.ItemPluggableSimple;
import buildcraft.transport.item.ItemWire;

@Mod.EventBusSubscriber(modid = BCTransport.MODID)
@GameRegistry.ObjectHolder(BCTransport.MODID)
public class BCTransportItems {
    public static final ItemBC_Neptune WATERPROOF = null;

    public static final ItemPipeHolder PIPE_STRUCTURE = null;

    public static final ItemPipeHolder PIPE_WOOD_ITEM = null;
    public static final ItemPipeHolder PIPE_WOOD_FLUID = null;
    public static final ItemPipeHolder PIPE_WOOD_POWER = null;

    public static final ItemPipeHolder PIPE_STONE_ITEM = null;
    public static final ItemPipeHolder PIPE_STONE_FLUID = null;
    public static final ItemPipeHolder PIPE_STONE_POWER = null;

    public static final ItemPipeHolder PIPE_COBBLE_ITEM = null;
    public static final ItemPipeHolder PIPE_COBBLE_FLUID = null;
    public static final ItemPipeHolder PIPE_COBBLE_POWER = null;

    public static final ItemPipeHolder PIPE_QUARTZ_ITEM = null;
    public static final ItemPipeHolder PIPE_QUARTZ_FLUID = null;
    public static final ItemPipeHolder PIPE_QUARTZ_POWER = null;

    public static final ItemPipeHolder PIPE_GOLD_ITEM = null;
    public static final ItemPipeHolder PIPE_GOLD_FLUID = null;
    public static final ItemPipeHolder PIPE_GOLD_POWER = null;

    public static final ItemPipeHolder PIPE_SANDSTONE_ITEM = null;
    public static final ItemPipeHolder PIPE_SANDSTONE_FLUID = null;
    public static final ItemPipeHolder PIPE_SANDSTONE_POWER = null;

    public static final ItemPipeHolder PIPE_IRON_ITEM = null;
    public static final ItemPipeHolder PIPE_IRON_FLUID = null;
    // public static final ItemPipeHolder pipePowerIron = null;

    public static final ItemPipeHolder PIPE_DIAMOND_ITEM = null;
    public static final ItemPipeHolder PIPE_DIAMOND_FLUID = null;
    // public static ItemPipeHolder pipePowerDiamond;

    @GameRegistry.ObjectHolder("pipe_diamond_wood_item")
    public static final ItemPipeHolder PIPE_DIAWOOD_ITEM = null;
    @GameRegistry.ObjectHolder("pipe_diamond_wood_fluid")
    public static final ItemPipeHolder PIPE_DIAWOOD_FLUID = null;

    public static final ItemPipeHolder PIPE_CLAY_ITEM = null;
    public static final ItemPipeHolder PIPE_CLAY_FLUID = null;

    public static final ItemPipeHolder PIPE_VOID_ITEM = null;
    public static final ItemPipeHolder PIPE_VOID_FLUID = null;
    public static final ItemPipeHolder PIPE_OBSIDIAN_ITEM = null;
    public static final ItemPipeHolder PIPE_OBSIDIAN_FLUID = null;

    public static final ItemPipeHolder PIPE_LAPIS_ITEM = null;
    public static final ItemPipeHolder PIPE_DAIZULI_ITEM = null;
    public static final ItemPipeHolder PIPE_EMZULI_ITEM = null;
    public static final ItemPipeHolder PIPE_STRIPES_ITEM = null;

    public static final ItemPluggableSimple PLUG_BLOCKER = null;
    public static final ItemPluggableGate PLUG_GATE = null;
    public static final ItemPluggableLens PLUG_LENS = null;
    public static final ItemPluggablePulsar PLUG_PULSAR = null;
    public static final ItemPluggableSimple PLUG_LIGHT_SENSOR = null;
    public static final ItemPluggableFacade PLUG_FACADE = null;

    public static final ItemWire WIRE = null;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        RegistryHelper.registerItems(event,
            new ItemBC_Neptune("item.waterproof"),
            // Register them in order of type -- item, fluid, power
            makePipeItem(BCTransportPipes.structure),
            makePipeItem(BCTransportPipes.woodItem),
            makePipeItem(BCTransportPipes.cobbleItem),
            makePipeItem(BCTransportPipes.stoneItem),
            makePipeItem(BCTransportPipes.quartzItem),
            makePipeItem(BCTransportPipes.ironItem),
            makePipeItem(BCTransportPipes.goldItem),
            makePipeItem(BCTransportPipes.clayItem),
            makePipeItem(BCTransportPipes.sandstoneItem),
            makePipeItem(BCTransportPipes.voidItem),
            makePipeItem(BCTransportPipes.obsidianItem),
            makePipeItem(BCTransportPipes.diamondItem),
            makePipeItem(BCTransportPipes.diaWoodItem),
            makePipeItem(BCTransportPipes.lapisItem),
            makePipeItem(BCTransportPipes.daizuliItem),
            makePipeItem(BCTransportPipes.emzuliItem),
            makePipeItem(BCTransportPipes.stripesItem),
            makePipeItem(BCTransportPipes.woodFluid),
            makePipeItem(BCTransportPipes.cobbleFluid),
            makePipeItem(BCTransportPipes.stoneFluid),
            makePipeItem(BCTransportPipes.quartzFluid),
            makePipeItem(BCTransportPipes.goldFluid),
            makePipeItem(BCTransportPipes.ironFluid),
            makePipeItem(BCTransportPipes.clayFluid),
            makePipeItem(BCTransportPipes.sandstoneFluid),
            makePipeItem(BCTransportPipes.voidFluid),
            makePipeItem(BCTransportPipes.diamondFluid),
            makePipeItem(BCTransportPipes.diaWoodFluid),
            //makePipeItem(BCTransportPipes.obsidianFluid),
            new ItemPluggableSimple("item.plug.blocker", BCTransportPlugs.blocker),
            new ItemPluggableGate("item.plug.gate"),
            new ItemPluggableLens("item.plug.lens"),
            new ItemPluggablePulsar("item.plug.pulsar"),
            new ItemPluggableSimple("item.plug.light_sensor", BCTransportPlugs.lightSensor),
            new ItemPluggableFacade("item.plug.facade"),
            new ItemWire("item.wire")
        );


        if (BCLib.DEV) {
            event.getRegistry().registerAll(
                makePipeItem(BCTransportPipes.woodPower),
                makePipeItem(BCTransportPipes.cobblePower),
                makePipeItem(BCTransportPipes.stonePower),
                makePipeItem(BCTransportPipes.quartzPower),
                makePipeItem(BCTransportPipes.goldPower)
                //makePipeItem(BCTransportPipes.ironPower),
                //makePipeItem(BCTransportPipes.sandstonePower)
            );
        }
    }

    public static ItemPipeHolder makePipeItem(PipeDefinition def) {
        ItemPipeHolder item = new ItemPipeHolder(def);
        item.registerWithPipeApi();
        return item;
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        RegistryHelper.registerVariants(
            WATERPROOF,
            PIPE_STRUCTURE,
            PIPE_WOOD_ITEM,
            PIPE_WOOD_FLUID,
            PIPE_WOOD_POWER,
            PIPE_STONE_ITEM,
            PIPE_STONE_FLUID,
            PIPE_STONE_POWER,
            PIPE_COBBLE_ITEM,
            PIPE_COBBLE_FLUID,
            PIPE_COBBLE_POWER,
            PIPE_QUARTZ_ITEM,
            PIPE_QUARTZ_FLUID,
            PIPE_QUARTZ_POWER,
            PIPE_GOLD_ITEM,
            PIPE_GOLD_FLUID,
            PIPE_GOLD_POWER,
            PIPE_SANDSTONE_ITEM,
            PIPE_SANDSTONE_FLUID,
            PIPE_SANDSTONE_POWER,
            PIPE_IRON_ITEM,
            PIPE_IRON_FLUID,
            PIPE_DIAMOND_ITEM,
            PIPE_DIAMOND_FLUID,
            PIPE_DIAWOOD_ITEM,
            PIPE_DIAWOOD_FLUID,
            PIPE_CLAY_ITEM,
            PIPE_CLAY_FLUID,
            PIPE_VOID_ITEM,
            PIPE_VOID_FLUID,
            PIPE_OBSIDIAN_ITEM,
            PIPE_OBSIDIAN_FLUID,
            PIPE_LAPIS_ITEM,
            PIPE_DAIZULI_ITEM,
            PIPE_EMZULI_ITEM,
            PIPE_STRIPES_ITEM,
            WIRE,
            PLUG_BLOCKER
        );
    }
}
