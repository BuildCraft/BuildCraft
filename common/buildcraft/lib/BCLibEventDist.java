/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.WorldServer;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.registry.EventBuildCraftReload;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.model.ModelHolderRegistry;
import buildcraft.lib.client.reload.ReloadManager;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.ClientDebuggables;
import buildcraft.lib.item.ItemDebugger;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.misc.FakePlayerProvider;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.net.MessageDebugRequest;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;

public enum BCLibEventDist {
    INSTANCE;

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) entity;
            // Delay sending join messages to player as it makes it work when in single-player
            MessageUtil.doDelayedServer(() -> MarkerCache.onPlayerJoinWorld(playerMP));
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        MarkerCache.onWorldUnload(event.getWorld());
        if (event.getWorld() instanceof WorldServer) {
            FakePlayerProvider.INSTANCE.unloadWorld((WorldServer) event.getWorld());
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onReloadFinish(EventBuildCraftReload.FinishLoad event) {
        // Note: when you need to add server-side listeners the client listeners need to be moved to BCLibProxy
        GuideManager.INSTANCE.onRegistryReload(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onConnectToServer(ClientConnectedToServerEvent event) {
        BuildCraftObjectCaches.onClientJoinServer();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void textureStitchPre(TextureStitchEvent.Pre event) {
        ReloadManager.INSTANCE.preReloadResources();
        TextureMap map = event.getMap();
        SpriteHolderRegistry.onTextureStitchPre(map);
        ModelHolderRegistry.onTextureStitchPre(map);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public static void textureStitchPreLow(TextureStitchEvent.Pre event) {
        FluidRenderer.onTextureStitchPre(event.getMap());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void textureStitchPost(TextureStitchEvent.Post event) {
        TextureMap map = event.getMap();
        SpriteHolderRegistry.onTextureStitchPost();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void modelBake(ModelBakeEvent event) {
        SpriteHolderRegistry.exportTextureMap();
        LaserRenderer_BC8.clearModels();
        ModelHolderRegistry.onModelBake();
        ModelVariableData.onModelBake();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void renderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;
        float partialTicks = event.getPartialTicks();

        DetachedRenderer.INSTANCE.renderWorldLastEvent(player, partialTicks);
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent event) {
        if (event.phase == Phase.END) {
            BCAdvDebugging.INSTANCE.onServerPostTick();
            MessageUtil.postServerTick();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void clientTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            BuildCraftObjectCaches.onClientTick();
            MessageUtil.postClientTick();
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.player;
            if (player != null && ItemDebugger.isShowDebugInfo(player)) {
                RayTraceResult mouseOver = mc.objectMouseOver;
                if (mouseOver != null) {
                    IDebuggable debuggable = ClientDebuggables.getDebuggableObject(mouseOver);
                    if (debuggable instanceof TileEntity) {
                        TileEntity tile = (TileEntity) debuggable;
                        MessageManager.sendToServer(new MessageDebugRequest(tile.getPos(), mouseOver.sideHit));
                    } else if (debuggable instanceof Entity) {
                        // TODO: Support entities!
                    }
                }
            }
        }
    }
}
