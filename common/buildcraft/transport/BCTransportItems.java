/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemPluggableSimple;
import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.transport.item.ItemPipeHolder;
import buildcraft.transport.item.ItemWire;
import buildcraft.transport.pipe.PipeRegistry;

public class BCTransportItems {
    private static final RegistrationHelper HELPER = new RegistrationHelper();

    public static ItemBC_Neptune waterproof;

    public static ItemPipeHolder pipeStructure;

    public static ItemPipeHolder pipeItemWood;
    public static ItemPipeHolder pipeFluidWood;
    public static ItemPipeHolder pipePowerWood;

    public static ItemPipeHolder pipeItemStone;
    public static ItemPipeHolder pipeFluidStone;
    public static ItemPipeHolder pipePowerStone;

    public static ItemPipeHolder pipeItemCobble;
    public static ItemPipeHolder pipeFluidCobble;
    public static ItemPipeHolder pipePowerCobble;

    public static ItemPipeHolder pipeItemQuartz;
    public static ItemPipeHolder pipeFluidQuartz;
    public static ItemPipeHolder pipePowerQuartz;

    public static ItemPipeHolder pipeItemGold;
    public static ItemPipeHolder pipeFluidGold;
    public static ItemPipeHolder pipePowerGold;

    public static ItemPipeHolder pipeItemSandstone;
    public static ItemPipeHolder pipeFluidSandstone;
    public static ItemPipeHolder pipePowerSandstone;

    public static ItemPipeHolder pipeItemIron;
    public static ItemPipeHolder pipeFluidIron;
    // public static ItemPipeHolder pipePowerIron;

    public static ItemPipeHolder pipeItemDiamond;
    public static ItemPipeHolder pipeFluidDiamond;
    // public static ItemPipeHolder pipePowerDiamond;

    public static ItemPipeHolder pipeItemDiaWood;
    public static ItemPipeHolder pipeFluidDiaWood;

    public static ItemPipeHolder pipeItemClay;
    public static ItemPipeHolder pipeFluidClay;

    public static ItemPipeHolder pipeItemVoid;
    public static ItemPipeHolder pipeFluidVoid;

    public static ItemPipeHolder pipeItemObsidian;
    public static ItemPipeHolder pipeFluidObsidian;

    public static ItemPipeHolder pipeItemLapis;
    public static ItemPipeHolder pipeItemDaizuli;
    public static ItemPipeHolder pipeItemEmzuli;
    public static ItemPipeHolder pipeItemStripes;

    public static ItemPluggableSimple plugBlocker;
    public static ItemPluggableSimple plugPowerAdaptor;

    public static ItemWire wire;

    public static void preInit() {
        waterproof = HELPER.addItem(new ItemBC_Neptune("item.waterproof"));

        pipeStructure = makePipeItem(BCTransportPipes.structure);

        // Register them in order of type -- item, fluid, power
        pipeItemWood = makePipeItem(BCTransportPipes.woodItem);
        pipeItemCobble = makePipeItem(BCTransportPipes.cobbleItem);
        pipeItemStone = makePipeItem(BCTransportPipes.stoneItem);
        pipeItemQuartz = makePipeItem(BCTransportPipes.quartzItem);
        pipeItemIron = makePipeItem(BCTransportPipes.ironItem);
        pipeItemGold = makePipeItem(BCTransportPipes.goldItem);
        pipeItemClay = makePipeItem(BCTransportPipes.clayItem);
        pipeItemSandstone = makePipeItem(BCTransportPipes.sandstoneItem);
        pipeItemVoid = makePipeItem(BCTransportPipes.voidItem);
        pipeItemObsidian = makePipeItem(BCTransportPipes.obsidianItem);
        pipeItemDiamond = makePipeItem(BCTransportPipes.diamondItem);
        pipeItemDiaWood = makePipeItem(BCTransportPipes.diaWoodItem);
        pipeItemLapis = makePipeItem(BCTransportPipes.lapisItem);
        pipeItemDaizuli = makePipeItem(BCTransportPipes.daizuliItem);
        pipeItemEmzuli = makePipeItem(BCTransportPipes.emzuliItem);
        pipeItemStripes = makePipeItem(BCTransportPipes.stripesItem);

        pipeFluidWood = makePipeItem(BCTransportPipes.woodFluid);
        pipeFluidCobble = makePipeItem(BCTransportPipes.cobbleFluid);
        pipeFluidStone = makePipeItem(BCTransportPipes.stoneFluid);
        pipeFluidQuartz = makePipeItem(BCTransportPipes.quartzFluid);
        pipeFluidGold = makePipeItem(BCTransportPipes.goldFluid);
        pipeFluidIron = makePipeItem(BCTransportPipes.ironFluid);
        pipeFluidClay = makePipeItem(BCTransportPipes.clayFluid);
        pipeFluidSandstone = makePipeItem(BCTransportPipes.sandstoneFluid);
        pipeFluidVoid = makePipeItem(BCTransportPipes.voidFluid);
        pipeFluidDiamond = makePipeItem(BCTransportPipes.diamondFluid);
        pipeFluidDiaWood = makePipeItem(BCTransportPipes.diaWoodFluid);
        // pipeFluidObsidian = makePipeItem(BCTransportPipes.obsidianFluid);

        pipePowerWood = makePipeItem(BCTransportPipes.woodPower);
        pipePowerCobble = makePipeItem(BCTransportPipes.cobblePower);
        pipePowerStone = makePipeItem(BCTransportPipes.stonePower);
        pipePowerQuartz = makePipeItem(BCTransportPipes.quartzPower);
        pipePowerGold = makePipeItem(BCTransportPipes.goldPower);
        // pipePowerIron = makePipeItem(BCTransportPipes.ironPower);
        pipePowerSandstone = makePipeItem(BCTransportPipes.sandstonePower);

        plugBlocker = HELPER.addItem(new ItemPluggableSimple("item.plug.blocker", BCTransportPlugs.blocker));
        plugPowerAdaptor = HELPER.addItem(new ItemPluggableSimple("item.plug.power_adaptor",
            BCTransportPlugs.powerAdaptor, ItemPluggableSimple.PIPE_BEHAVIOUR_ACCEPTS_RS_POWER));

        wire = HELPER.addItem(new ItemWire("item.wire"));
    }

    public static ItemPipeHolder makePipeItem(PipeDefinition def) {
        return PipeRegistry.INSTANCE.createItemForPipe(def);
    }
}
