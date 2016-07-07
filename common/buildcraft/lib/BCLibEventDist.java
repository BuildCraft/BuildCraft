/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.DetatchedRenderer;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.misc.MessageUtil;

public enum BCLibEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) entity;
            // Delay sending join messages to player as it makes it work when in single-player
            MessageUtil.doDelayed(() -> MarkerCache.onPlayerJoinWorld(playerMP));
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        MarkerCache.onWorldUnload(event.getWorld());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onConnectToServer(ClientConnectedToServerEvent event) {
        BCLibDatabase.connectToServer();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void textureStitchPre(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        SpriteHolderRegistry.onTextureStitchPre(map);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void textureStitchPost(TextureStitchEvent.Post event) {
        TextureMap map = event.getMap();

    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void modelBake(ModelBakeEvent event) {
        LaserRenderer_BC8.clearModels();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void renderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (player == null) return;
        float partialTicks = event.getPartialTicks();

        DetatchedRenderer.INSTANCE.renderWorldLastEvent(player, partialTicks);
    }

    @SubscribeEvent
    public void serverTick(ServerTickEvent event) {
        if (event.phase == Phase.START) {
            MessageUtil.preTick();
        }
    }

    @SubscribeEvent
    public void clientTick(ClientTickEvent event) {
        if (event.phase == Phase.START) {
            MessageUtil.preTick();
        }
    }

    public static void onServerStarted(FMLServerStartedEvent started) {
        BCLibDatabase.onServerStarted();
    }
}
