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

import net.minecraftforge.common.MinecraftForge;

import buildcraft.api.core.EnumHandlerPriority;
import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeConnectionAPI;
import buildcraft.api.transport.pipe.PipeFlowType;

import buildcraft.transport.pipe.PipeRegistry;
import buildcraft.transport.pipe.StripesRegistry;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowStructure;
import buildcraft.transport.plug.FacadeStateManager;
import buildcraft.transport.plug.PluggableRegistry;
import buildcraft.transport.stripes.IPipeExtensionManager;
import buildcraft.transport.stripes.PipeExtensionManager;
import buildcraft.transport.stripes.StripesHandlerDispenser;
import buildcraft.transport.stripes.StripesHandlerEntityInteract;
import buildcraft.transport.stripes.StripesHandlerHoe;
import buildcraft.transport.stripes.StripesHandlerMinecartDestroy;
import buildcraft.transport.stripes.StripesHandlerPipes;
import buildcraft.transport.stripes.StripesHandlerPlaceBlock;
import buildcraft.transport.stripes.StripesHandlerPlant;
import buildcraft.transport.stripes.StripesHandlerShears;
import buildcraft.transport.stripes.StripesHandlerUse;

public class BCTransportRegistries {

    // TODO: Move to API
    public static IPipeExtensionManager extensionManager;

    public static void preInit() {
        FacadeAPI.registry = FacadeStateManager.INSTANCE;

        PipeApi.pipeRegistry = PipeRegistry.INSTANCE;
        PipeApi.pluggableRegistry = PluggableRegistry.INSTANCE;
        PipeApi.stripeRegistry = StripesRegistry.INSTANCE;
        // TODO: Move to API
        BCTransportRegistries.extensionManager = PipeExtensionManager.INSTANCE;
        MinecraftForge.EVENT_BUS.register(PipeExtensionManager.INSTANCE);

        PipeApi.flowItems = new PipeFlowType(PipeFlowItems::new, PipeFlowItems::new);
        PipeApi.flowFluids = new PipeFlowType(PipeFlowFluids::new, PipeFlowFluids::new);
        PipeApi.flowPower = new PipeFlowType(PipeFlowPower::new, PipeFlowPower::new);
        PipeApi.flowStructure = new PipeFlowType(PipeFlowStructure::new, PipeFlowStructure::new);
    }

    public static void init() {
        PipeConnectionAPI.registerConnection(
            Blocks.BREWING_STAND,
            (world, pos, face, state) -> face.getAxis().getPlane() == EnumFacing.Plane.HORIZONTAL ? 4 / 16F : 0
        );

        // Item use stripes handlers
        PipeApi.stripeRegistry.addHandler(StripesHandlerPlant.INSTANCE);
        PipeApi.stripeRegistry.addHandler(StripesHandlerShears.INSTANCE);
        PipeApi.stripeRegistry.addHandler(new StripesHandlerPipes());
        // PipeApi.stripeRegistry.addHandler(new StripesHandlerPipeWires());
        PipeApi.stripeRegistry.addHandler(StripesHandlerEntityInteract.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerHoe.INSTANCE);
        // PipeApi.stripeRegistry.addHandler(new StripesHandlerRightClick(), EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerDispenser.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerPlaceBlock.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerUse.INSTANCE, EnumHandlerPriority.LOW);

        StripesHandlerDispenser.ITEM_CLASSES.add(ItemBucket.class);
        StripesHandlerDispenser.ITEM_CLASSES.add(ItemMinecart.class);
        // StripesHandlerRightClick.items.add(Items.EGG);
        // StripesHandlerRightClick.items.add(Items.SNOWBALL);
        // StripesHandlerRightClick.items.add(Items.EXPERIENCE_BOTTLE);
        StripesHandlerUse.ITEMS.add(Items.FIREWORKS);

        // Block breaking stripes handlers
        PipeApi.stripeRegistry.addHandler(StripesHandlerMinecartDestroy.INSTANCE);

        BCTransportRegistries.extensionManager.registerRetractionPipe(BCTransportPipes.voidItem);
    }
}
