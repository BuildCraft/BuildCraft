/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.WorldServer;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.client.model.ModelHolderRegistry;
import buildcraft.lib.client.reload.ReloadManager;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.ClientDebuggables;
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
            MessageUtil.doDelayed(() -> MarkerCache.onPlayerJoinWorld(playerMP));
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
    public static void onConnectToServer(ClientConnectedToServerEvent event) {
        BuildCraftObjectCaches.onClientJoinServer();
        // Really obnoxious warning
        if (!BCLib.DEV) {
            /* If people are in a dev environment or have toggled the flag then they probably already know about this */
            Runnable r = () -> {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    // NO-OP
                }

                String ver;
                if (BCLib.VERSION.startsWith("${")) {
                    ModContainer mod = Loader.instance().getIndexedModList().get(BCLib.MODID);
                    if (mod == null) {
                        ver = "[UNKNOWN-MANUAL-BUILD]";
                    } else {
                        ver = mod.getDisplayVersion();
                        if (ver.startsWith("${")) {
                            // The difference with the above is intentional
                            ver = "[UNKNOWN_MANUAL_BUILD]";
                        }
                    }
                } else {
                    ver = BCLib.VERSION;
                }

                ITextComponent componentVersion = new TextComponentString(ver);
                Style styleVersion = new Style();
                styleVersion.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, BCLib.VERSION));
                // styleVersion.setHoverEvent(new HoverEvent(HoverEvent.Action., valueIn));
                componentVersion.setStyle(styleVersion);

                String githubIssuesUrl = "https://github.com/BuildCraft/BuildCraft/issues";
                ITextComponent componentGithubLink = new TextComponentString("here");
                Style styleGithubLink = new Style();
                styleGithubLink.setUnderlined(Boolean.TRUE);
                styleGithubLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, githubIssuesUrl));
                componentGithubLink.setStyle(styleGithubLink);

                TextComponentString textWarn = new TextComponentString("WARNING: BuildCraft ");
                textWarn.appendSibling(componentVersion);
                textWarn.appendText(" is in ALPHA!");

                TextComponentString textReport = new TextComponentString("  Report bugs you find ");
                textReport.appendSibling(componentGithubLink);

                TextComponentString textDesc = new TextComponentString("  and include the version ");
                textDesc.appendSibling(componentVersion);
                textDesc.appendText(" in the description");

                ITextComponent[] lines = { textWarn, textReport, textDesc };
                GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
                for (ITextComponent line : lines) {
                    chat.printChatMessage(line);
                }
            };
            new Thread(r).start();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void textureStitchPre(TextureStitchEvent.Pre event) {
        ReloadManager.INSTANCE.preReloadResources();
        TextureMap map = event.getMap();
        SpriteHolderRegistry.onTextureStitchPre(map);
        ModelHolderRegistry.onTextureStitchPre(map);
        FluidRenderer.onTextureStitchPre(map);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void textureStitchPost(TextureStitchEvent.Post event) {
        TextureMap map = event.getMap();
        SpriteHolderRegistry.onTextureStitchPost();
        FluidRenderer.onTextureStitchPost(map);
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
            MessageUtil.postTick();
        }
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            BuildCraftObjectCaches.onClientTick();
            MessageUtil.postTick();
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.player;
            if (player != null && player.capabilities.isCreativeMode && mc.gameSettings.showDebugInfo) {
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
