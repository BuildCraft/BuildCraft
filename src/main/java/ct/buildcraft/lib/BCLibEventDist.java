/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.lib.client.model.ModelHolderRegistry;
import ct.buildcraft.lib.client.model.json.VariablePartLed;
import ct.buildcraft.lib.client.reload.LibConfigChangeListener;
import ct.buildcraft.lib.client.reload.ReloadManager;
import ct.buildcraft.lib.client.render.DetachedRenderer;
import ct.buildcraft.lib.client.render.DetachedRenderer.RenderMatrixType;
import ct.buildcraft.lib.client.render.MarkerRenderer;
import ct.buildcraft.lib.client.render.fluid.FluidRenderer;
import ct.buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry;
import ct.buildcraft.lib.debug.BCAdvDebugging;
import ct.buildcraft.lib.debug.ClientDebuggables;
import ct.buildcraft.lib.item.ItemDebugger;
import ct.buildcraft.lib.marker.MarkerCache;
import ct.buildcraft.lib.misc.FakePlayerProvider;
import ct.buildcraft.lib.misc.MessageUtil;
import ct.buildcraft.lib.net.MessageDebugRequest;
import ct.buildcraft.lib.net.MessageDebugResponse;
import ct.buildcraft.lib.net.MessageManager;
import ct.buildcraft.lib.net.MessageMarker;
import ct.buildcraft.lib.net.cache.BuildCraftObjectCaches;
import ct.buildcraft.lib.net.cache.MessageObjectCacheResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.client.event.RegisterTextureAtlasSpriteLoadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


public class BCLibEventDist {
	
	@Mod.EventBusSubscriber(modid = BCLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class Client {
		
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
/*            ReloadableRegistryManager manager = ReloadableRegistryManager.RESOURCE_PACKS;
            BuildCraftRegistryManager.managerResourcePacks = manager;
            manager.registerRegistry(GuidePageRegistry.INSTANCE);*/

            DetachedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, MarkerRenderer.INSTANCE);
            // various sprite registers
            BCLibSprites.fmlPreInitClient();
            BCLibConfig.configChangeListeners.add(LibConfigChangeListener.INSTANCE);

            MessageManager.setHandler(MessageMarker.class, MessageMarker.HANDLER, Dist.CLIENT);
            MessageManager.setHandler(MessageObjectCacheResponse.class, MessageObjectCacheResponse.HANDLER, Dist.CLIENT);
            MessageManager.setHandler(MessageDebugResponse.class, MessageDebugResponse.HANDLER, Dist.CLIENT);
        }
        
        @SubscribeEvent
        public static void onTextureAtlasSpriteLoadersSetup(RegisterTextureAtlasSpriteLoadersEvent event)
        {
        }
		
	    @SubscribeEvent//(priority = EventPriority.HIGHEST)
	    public static void textureStitchPre(TextureStitchEvent.Pre event) {
	    	if("textures/atlas/blocks.png".equals(event.getAtlas().location().getPath())) {
	    		ReloadManager.INSTANCE.preReloadResources();
	    		SpriteHolderRegistry.onTextureStitchPre(event);
	    		ModelHolderRegistry.onTextureStitchPre(event);
	    		FluidRenderer.onTextureStitchPre(event);
	    	}
	    }
	
/*	    @SubscribeEvent(priority = EventPriority.LOWEST)
	    @OnlyIn(Dist.CLIENT)
	    public static void textureStitchPreLow(TextureStitchEvent.Pre event) {
	    	if("textures/atlas/blocks.png".equals(event.getAtlas().location().getPath())) {
	    		
	    	}
	        
	    }*/

	    @SubscribeEvent
	    public static void textureStitchPost(TextureStitchEvent.Post event) {
	    	if("textures/atlas/blocks.png".equals(event.getAtlas().location().getPath()))
	        SpriteHolderRegistry.onTextureStitchPost(event);
	    	VariablePartLed.onTextureStitchPost(event);
	    }
	    
	    @SubscribeEvent
	    public static void preModelBake(RegisterAdditional event) {
	    	ModelHolderRegistry.preModelBake(event);
	    }
	
	    @SubscribeEvent
	    public static void onModelBake(BakingCompleted event) {
	        SpriteHolderRegistry.exportTextureMap();
	        LaserRenderer_BC8.clearModels();
	        ModelHolderRegistry.onModelBake(event);
	    }

	    
	}
	/*	    @SubscribeEvent
    public static void renderWorldLast(RenderLevelLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        float partialTicks = event.getPartialTicks();

//        DetachedRenderer.INSTANCE.renderWorldLastEvent(player, partialTicks);
    }
*/
	@SubscribeEvent
    public static void renderWorldLast(RenderLevelStageEvent event) {
    	if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
    		return ;
    	}
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        PoseStack pose = event.getPoseStack();
        Matrix4f matrix = event.getProjectionMatrix();
        float partialTicks = event.getPartialTick();
        
        DetachedRenderer.INSTANCE.renderWorldLastEvent(pose, matrix, player, partialTicks);
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer) {
            ServerPlayer playerMP = (ServerPlayer) entity;
            // Delay sending join messages to player as it makes it work when in single-player
            MessageUtil.doDelayedServer(() -> MarkerCache.onPlayerJoinLevel(playerMP));
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        MarkerCache.onLevelUnload(event.getLevel());
        if (event.getLevel() instanceof ServerLevel) {
            FakePlayerProvider.INSTANCE.unloadWorld((ServerLevel) event.getLevel());
        }
    }
/*
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onReloadFinish(EventBuildCraftReload.FinishLoad event) {
        // Note: when you need to add server-side listeners the client listeners need to be moved to BCLibProxy
        GuideManager.INSTANCE.onRegistryReload(event);
    }
*/
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onConnectToServer(ClientPlayerNetworkEvent.LoggingIn event) {
        BuildCraftObjectCaches.onClientJoinServer();
    }


    @SubscribeEvent
    public static void serverTick(ServerTickEvent event) {
        if (event.phase == Phase.END) {
            BCAdvDebugging.INSTANCE.onServerPostTick();
            MessageUtil.postServerTick();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void clientTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            BuildCraftObjectCaches.onClientTick();
            MessageUtil.postClientTick();
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null && ItemDebugger.isShowDebugInfo(player)) {
                HitResult mouseOver = mc.hitResult;
                if (mouseOver != null) {
                    IDebuggable debuggable = ClientDebuggables.getDebuggableObject(mouseOver);
                    if (debuggable instanceof BlockEntity) {
                        BlockEntity tile = (BlockEntity) debuggable;
                        MessageManager.sendToServer(new MessageDebugRequest(tile.getBlockPos(), Direction.getNearest(mouseOver.getLocation().x, mouseOver.getLocation().y, mouseOver.getLocation().z)));
                    } else if (debuggable instanceof Entity) {
                        // TODO: Support entities!
                    }
                }
            }
        }
    }
}
