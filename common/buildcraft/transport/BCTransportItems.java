/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import java.util.ArrayList;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.item.ItemBC_Neptune;

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
    public static ItemBC_Neptune waterproof;

    public static final ItemPipeHolder pipeStructure = null;

    public static final ItemPipeHolder pipeItemWood  = null;
    public static final ItemPipeHolder pipeFluidWood  = null;
    public static final ItemPipeHolder pipePowerWood  = null;

    public static final ItemPipeHolder pipeItemStone  = null;
    public static final ItemPipeHolder pipeFluidStone  = null;
    public static final ItemPipeHolder pipePowerStone  = null;

    public static final ItemPipeHolder pipeItemCobble  = null;
    public static final ItemPipeHolder pipeFluidCobble  = null;
    public static final ItemPipeHolder pipePowerCobble  = null;

    public static final ItemPipeHolder pipeItemQuartz  = null;
    public static final ItemPipeHolder pipeFluidQuartz  = null;
    public static final ItemPipeHolder pipePowerQuartz  = null;

    public static final ItemPipeHolder pipeItemGold = null;
    public static final ItemPipeHolder pipeFluidGold = null;
    public static final ItemPipeHolder pipePowerGold = null;

    public static final ItemPipeHolder pipeItemSandstone = null;
    public static final ItemPipeHolder pipeFluidSandstone = null;
    public static final ItemPipeHolder pipePowerSandstone = null;

    public static final ItemPipeHolder pipeItemIron = null;
    public static final ItemPipeHolder pipeFluidIron = null;
    // public static ItemPipeHolder pipePowerIron;

    public static final ItemPipeHolder pipeItemDiamond = null;
    public static final ItemPipeHolder pipeFluidDiamond = null;
    // public static ItemPipeHolder pipePowerDiamond;

    public static final ItemPipeHolder pipeItemDiaWood = null;
    public static final ItemPipeHolder pipeFluidDiaWood = null;

    public static final ItemPipeHolder pipeItemClay = null;
    public static final ItemPipeHolder pipeFluidClay = null;

    public static final ItemPipeHolder pipeItemVoid = null;
    public static final ItemPipeHolder pipeFluidVoid = null;

    public static final ItemPipeHolder pipeItemObsidian = null;
    public static final ItemPipeHolder pipeFluidObsidian = null;

    public static final ItemPipeHolder pipeItemLapis = null;
    public static final ItemPipeHolder pipeItemDaizuli = null;
    public static final ItemPipeHolder pipeItemEmzuli = null;
    public static final ItemPipeHolder pipeItemStripes = null;

    @GameRegistry.ObjectHolder("plug_blocker")
    public static final ItemPluggableSimple plugBlocker = null;
    @GameRegistry.ObjectHolder("plug_gate")
    public static final ItemPluggableGate plugGate = null;
    @GameRegistry.ObjectHolder("plug_lens")
    public static final ItemPluggableLens plugLens = null;
    @GameRegistry.ObjectHolder("plug_pulsar")
    public static final ItemPluggablePulsar plugPulsar = null;
    @GameRegistry.ObjectHolder("plug_light_sensor")
    public static final ItemPluggableSimple plugLightSensor = null;
    @GameRegistry.ObjectHolder("plug_facade")
    public static final ItemPluggableFacade plugFacade = null;

    public static final ItemWire wire = null;

    private static ArrayList<IItemBuildCraft> items = new ArrayList<>();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        BCTransportPipes.preInit();
        BCTransportPlugs.preInit();
        listAndRegister(event,
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

    private static <T extends Item & IItemBuildCraft> void listAndRegister(RegistryEvent.Register<Item> event, T...bcItems) {
        for (T item: bcItems) {
            event.getRegistry().register(item);
            items.add(item);
        }
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelRegistryEvent event) {
        items.forEach(IItemBuildCraft::registerVariants);
    }
}
