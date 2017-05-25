/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemMinecart;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IStripesRegistry.HandlerPriority;
import buildcraft.api.transport.pipe.ICustomPipeConnection;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeConnectionAPI;
import buildcraft.api.transport.pipe.PipeFlowType;

import buildcraft.transport.pipe.PipeRegistry;
import buildcraft.transport.pipe.StripesRegistry;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowStructure;
import buildcraft.transport.plug.PluggableRegistry;
import buildcraft.transport.stripes.*;

public class BCTransportRegistries {
    public static void preInit() {
        PipeApi.pipeRegistry = PipeRegistry.INSTANCE;
        PipeApi.pluggableRegistry = PluggableRegistry.INSTANCE;
        PipeApi.stripeRegistry = StripesRegistry.INSTANCE;

        PipeApi.flowItems = new PipeFlowType(PipeFlowItems::new, PipeFlowItems::new);
        PipeApi.flowFluids = new PipeFlowType(PipeFlowFluids::new, PipeFlowFluids::new);
        PipeApi.flowPower = new PipeFlowType(PipeFlowPower::new, PipeFlowPower::new);
        PipeApi.flowStructure = new PipeFlowType(PipeFlowStructure::new, PipeFlowStructure::new);
    }

    public static void init() {
        ICustomPipeConnection smallerBlockConnection = (world, pos, face, state) -> face == EnumFacing.UP ? 0 : 2 / 16f;
        PipeConnectionAPI.registerConnection(Blocks.CHEST, smallerBlockConnection);
        PipeConnectionAPI.registerConnection(Blocks.TRAPPED_CHEST, smallerBlockConnection);
        PipeConnectionAPI.registerConnection(Blocks.HOPPER, smallerBlockConnection);

        // Item use stripes handlers
        PipeApi.stripeRegistry.addHandler(StripesHandlerPlant.INSTANCE);
        PipeApi.stripeRegistry.addHandler(StripesHandlerShears.INSTANCE);
        // PipeApi.stripeRegistry.addHandler(new StripesHandlerPipes());
        // PipeApi.stripeRegistry.addHandler(new StripesHandlerPipeWires());
        PipeApi.stripeRegistry.addHandler(StripesHandlerEntityInteract.INSTANCE, HandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerHoe.INSTANCE);
        // PipeApi.stripeRegistry.addHandler(new StripesHandlerRightClick(), HandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerDispenser.INSTANCE, HandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerPlaceBlock.INSTANCE, HandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerUse.INSTANCE, HandlerPriority.LOW);

        StripesHandlerDispenser.ITEM_CLASSES.add(ItemBucket.class);
        StripesHandlerDispenser.ITEM_CLASSES.add(ItemMinecart.class);
        // StripesHandlerRightClick.items.add(Items.EGG);
        // StripesHandlerRightClick.items.add(Items.SNOWBALL);
        // StripesHandlerRightClick.items.add(Items.EXPERIENCE_BOTTLE);
        StripesHandlerUse.ITEMS.add(Items.FIREWORKS);

        // Block breaking stripes handlers
        PipeApi.stripeRegistry.addHandler(StripesHandlerMinecartDestroy.INSTANCE);
    }
}
