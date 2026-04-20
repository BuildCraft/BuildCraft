/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport;

import ct.buildcraft.api.core.EnumHandlerPriority;
import ct.buildcraft.api.transport.pipe.EnumPipeColourType;
import ct.buildcraft.api.transport.pipe.PipeApi;
import ct.buildcraft.api.transport.pipe.PipeConnectionAPI;
import ct.buildcraft.api.transport.pipe.PipeFlowType;
import ct.buildcraft.transport.pipe.PipeRegistry;
import ct.buildcraft.transport.pipe.StripesRegistry;
import ct.buildcraft.transport.pipe.flow.PipeFlowFluids;
import ct.buildcraft.transport.pipe.flow.PipeFlowItems;
import ct.buildcraft.transport.pipe.flow.PipeFlowPower;
import ct.buildcraft.transport.pipe.flow.PipeFlowStructure;
import ct.buildcraft.transport.stripes.PipeExtensionManager;
import ct.buildcraft.transport.stripes.StripesHandlerDispenser;
import ct.buildcraft.transport.stripes.StripesHandlerEntityInteract;
import ct.buildcraft.transport.stripes.StripesHandlerHoe;
import ct.buildcraft.transport.stripes.StripesHandlerMinecartDestroy;
import ct.buildcraft.transport.stripes.StripesHandlerPipes;
import ct.buildcraft.transport.stripes.StripesHandlerPlaceBlock;
import ct.buildcraft.transport.stripes.StripesHandlerPlant;
import ct.buildcraft.transport.stripes.StripesHandlerShears;
import ct.buildcraft.transport.stripes.StripesHandlerUse;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;

public class BCTransportRegistries {

    public static void preInit() {
        PipeApi.pipeRegistry = PipeRegistry.INSTANCE;
        PipeApi.stripeRegistry = StripesRegistry.INSTANCE;
        PipeApi.extensionManager = PipeExtensionManager.INSTANCE;
        MinecraftForge.EVENT_BUS.register(PipeExtensionManager.INSTANCE);

        PipeApi.flowItems = new PipeFlowType(PipeFlowItems::new, PipeFlowItems::new);
        PipeApi.flowFluids = new PipeFlowType(PipeFlowFluids::new, PipeFlowFluids::new);
        PipeApi.flowPower = new PipeFlowType(PipeFlowPower::new, PipeFlowPower::new);
        PipeApi.flowStructure = new PipeFlowType(PipeFlowStructure::new, PipeFlowStructure::new);
        PipeApi.flowStructure.fallbackColourType = EnumPipeColourType.BORDER_OUTER;
    }

    public static void init() {
        PipeConnectionAPI.registerConnection(Blocks.BREWING_STAND,
            (world, pos, face, state) -> face.getAxis().getPlane() == Direction.Plane.HORIZONTAL ? 4 / 16F : 0);

        // Item use stripes handlers
        PipeApi.stripeRegistry.addHandler(StripesHandlerPlant.INSTANCE);
        PipeApi.stripeRegistry.addHandler(StripesHandlerShears.INSTANCE);
        PipeApi.stripeRegistry.addHandler(new StripesHandlerPipes());
        // PipeApi.stripeRegistry.addHandler(new StripesHandlerPipeWires());
        PipeApi.stripeRegistry.addHandler(StripesHandlerEntityInteract.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerHoe.INSTANCE);
        // PipeApi.stripeRegistry.addHandler(new StripesHandlerRightClick(), InteractionHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerDispenser.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerPlaceBlock.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerUse.INSTANCE, EnumHandlerPriority.LOW);

        // For testing
        // StripesHandlerDispenser.ITEM_CLASSES.add(ItemBucket.class);
        // StripesHandlerDispenser.ITEM_CLASSES.add(ItemMinecart.class);

        // StripesHandlerRightClick.items.add(Items.EGG);
        // StripesHandlerRightClick.items.add(Items.SNOWBALL);
        // StripesHandlerRightClick.items.add(Items.EXPERIENCE_BOTTLE);

        // Block breaking stripes handlers
        PipeApi.stripeRegistry.addHandler(StripesHandlerMinecartDestroy.INSTANCE);

        PipeApi.extensionManager.registerRetractionPipe(BCTransportPipes.voidItem);
    }
}
