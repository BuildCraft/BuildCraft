/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport;

import ct.buildcraft.api.transport.pipe.PipeApiClient;
import ct.buildcraft.transport.client.PipeRegistryClient;
import ct.buildcraft.transport.client.model.PipeBaseModelGenStandard;
import ct.buildcraft.transport.client.render.PipeWireRenderer;
import ct.buildcraft.transport.net.PipeItemMessageQueue;
import ct.buildcraft.transport.wire.WorldSavedDataWireSystems;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class BCTransportEventDist {

    @Mod.EventBusSubscriber(modid = BCTransport.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
    	public static final ResourceLocation TRUNK_LIGHT = new ResourceLocation("buildcraftcore:blocks/engine/trunk_light");
    	public static final ResourceLocation CHAMBER = new ResourceLocation("buildcraftcore:blocks/engine/chamber_base");
    	
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        	BCTransportModels.init();
        }
        
        @SubscribeEvent
        public static void onClientCommonSetup(FMLCommonSetupEvent event)
        {
        	PipeApiClient.registry = PipeRegistryClient.INSTANCE;
        	BCTransportGuis.clientInit(event);
        }
        
        @SubscribeEvent
        public static void registryRender(EntityRenderersEvent.RegisterRenderers e) {
        	BCTransportModels.onBlockEntityRender(e);
        }
        
        @SubscribeEvent
        public static void onBlockColor(RegisterColorHandlersEvent.Block event) {
        	BCTransportModels.onBlockColor(event);
        }
        
        @SubscribeEvent
        public static void onModelBakePre(RegisterAdditional event) {
        	BCTransportModels.onModelBakePre(event);
        }
        
        @SubscribeEvent
        public static void onModelBake(BakingCompleted event) {
        	BCTransportModels.onModelBake(event);
        	PipeBaseModelGenStandard.loadSpritesCache();
        }
        
        @SubscribeEvent
        public static void registryTexture(TextureStitchEvent.Pre e){ 
        	BCTransportSprites.onTextureStitchPre(e);
        	PipeWireRenderer.onTextureStitchPre();
        }
        
    }
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (!event.level.isClientSide && event.level.getServer() != null) {
            WorldSavedDataWireSystems.get(event.level).tick();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        PipeItemMessageQueue.serverTick();
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent event) {
        WorldSavedDataWireSystems.get(event.getPlayer().level).changedPlayers.add(event.getPlayer());
    }


    @SubscribeEvent
    public static void onBlockPlace(EntityPlaceEvent event) {
        // event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // event.setCanceled(true);
    }
}
