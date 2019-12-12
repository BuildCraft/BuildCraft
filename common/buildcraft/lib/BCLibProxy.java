/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;

import buildcraft.api.BCModules;
import buildcraft.api.registry.BuildCraftRegistryManager;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.GuidePageRegistry;
import buildcraft.lib.client.reload.LibConfigChangeListener;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.DetachedRenderer.RenderMatrixType;
import buildcraft.lib.client.render.MarkerRenderer;
import buildcraft.lib.debug.DebugRenderHelper;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.item.ItemGuide;
import buildcraft.lib.net.MessageContainer;
import buildcraft.lib.net.MessageDebugRequest;
import buildcraft.lib.net.MessageDebugResponse;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.MessageMarker;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.cache.MessageObjectCacheRequest;
import buildcraft.lib.net.cache.MessageObjectCacheResponse;
import buildcraft.lib.script.ReloadableRegistryManager;

public abstract class BCLibProxy implements IGuiHandler {
    @SidedProxy(modId = BCLib.MODID)
    private static BCLibProxy proxy;

    public static BCLibProxy getProxy() {
        return proxy;
    }

    void fmlPreInit() {
        MessageManager.registerMessageClass(BCModules.LIB, MessageUpdateTile.class, MessageUpdateTile.HANDLER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageContainer.class, MessageContainer.HANDLER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageMarker.class, Side.CLIENT);
        MessageManager.registerMessageClass(BCModules.LIB, MessageObjectCacheRequest.class,
            MessageObjectCacheRequest.HANDLER, Side.SERVER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageObjectCacheResponse.class, Side.CLIENT);
        MessageManager.registerMessageClass(BCModules.LIB, MessageDebugRequest.class, MessageDebugRequest.HANDLER,
            Side.SERVER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageDebugResponse.class, Side.CLIENT);
    }

    void fmlInit() {}

    void fmlPostInit() {}

    public World getClientWorld() {
        return null;
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    public EntityPlayer getPlayerForContext(MessageContext ctx) {
        return ctx.getServerHandler().player;
    }

    public void addScheduledTask(World world, Runnable task) {
        if (world instanceof WorldServer) {
            WorldServer server = (WorldServer) world;
            server.addScheduledTask(task);
        }
    }

    public <T extends TileEntity> T getServerTile(T tile) {
        return tile;
    }

    public InputStream getStreamForIdentifier(ResourceLocation identifier) throws IOException {
        return null;
    }

    public abstract File getGameDirectory();

    public Iterable<File> getLoadedResourcePackFiles() {
        return Collections.emptySet();
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @SuppressWarnings("unused")
    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCLibProxy {
        @Override
        public File getGameDirectory() {
            return FMLServerHandler.instance().getServer().getDataDirectory();
        }
    }

    @SuppressWarnings("unused")
    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCLibProxy {
        @Override
        void fmlPreInit() {
            super.fmlPreInit();

            ReloadableRegistryManager manager = ReloadableRegistryManager.RESOURCE_PACKS;
            BuildCraftRegistryManager.managerResourcePacks = manager;
            manager.registerRegistry(GuidePageRegistry.INSTANCE);

            DetachedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, MarkerRenderer.INSTANCE);
            DetachedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, DebugRenderHelper.INSTANCE);
            // various sprite registers
            BCLibSprites.fmlPreInitClient();
            BCLibConfig.configChangeListeners.add(LibConfigChangeListener.INSTANCE);

            MessageManager.setHandler(MessageMarker.class, MessageMarker.HANDLER, Side.CLIENT);
            MessageManager.setHandler(MessageObjectCacheResponse.class, MessageObjectCacheResponse.HANDLER,
                Side.CLIENT);
            MessageManager.setHandler(MessageDebugResponse.class, MessageDebugResponse.HANDLER, Side.CLIENT);
        }

        @Override
        void fmlInit() {
            super.fmlInit();
        }

        @Override
        void fmlPostInit() {
            super.fmlPostInit();
            if (BCLibItems.isGuideEnabled()) {
                IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
                IReloadableResourceManager reloadable = (IReloadableResourceManager) manager;
                reloadable.registerReloadListener(GuideManager.INSTANCE);
            }
            GuiConfigManager.loadFromConfigFile();
        }

        @Override
        public World getClientWorld() {
            return Minecraft.getMinecraft().world;
        }

        @Override
        public EntityPlayer getClientPlayer() {
            return Minecraft.getMinecraft().player;
        }

        @Override
        public EntityPlayer getPlayerForContext(MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                return super.getPlayerForContext(ctx);
            }
            return getClientPlayer();
        }

        @Override
        public void addScheduledTask(World world, Runnable task) {
            if (world instanceof WorldClient) {
                Minecraft.getMinecraft().addScheduledTask(task);
            } else {
                super.addScheduledTask(world, task);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends TileEntity> T getServerTile(T tile) {
            if (tile != null && tile.hasWorld()) {
                World world = tile.getWorld();
                if (world.isRemote && Minecraft.getMinecraft().isSingleplayer()) {
                    WorldServer server = DimensionManager.getWorld(world.provider.getDimension());
                    if (server == null) return tile;
                    TileEntity atServer = server.getTileEntity(tile.getPos());
                    if (atServer == null) return tile;
                    if (atServer.getClass() == tile.getClass()) {
                        return (T) atServer;
                    }
                }
            }
            return tile;
        }

        @Override
        public File getGameDirectory() {
            return Minecraft.getMinecraft().mcDataDir;
        }

        @Override
        public Iterable<File> getLoadedResourcePackFiles() {
            List<File> files = new ArrayList<>();
            for (ResourcePackRepository.Entry entry : Minecraft.getMinecraft().getResourcePackRepository()
                .getRepositoryEntries()) {
                IResourcePack pack = entry.getResourcePack();
                if (pack instanceof AbstractResourcePack) {
                    AbstractResourcePack p = (AbstractResourcePack) pack;
                    Object f = ObfuscationReflectionHelper.getPrivateValue(AbstractResourcePack.class, p, 1);
                    if (!(f instanceof File)) {
                        throw new Error("We've got the wrong field! (Expected a file but got " + f + ")");
                    }
                    files.add((File) f);
                }
            }
            return files;
        }

        @Override
        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            if (id == 0) {
                EnumHand hand = x == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                ItemStack stack = player.getHeldItem(hand);
                String name = ItemGuide.getBookName(stack);
                if (name == null) {
                    return new GuiGuide();
                } else {
                    return new GuiGuide(name);
                }
            }
            return null;
        }

        @Override
        public InputStream getStreamForIdentifier(ResourceLocation identifier) throws IOException {
            return Minecraft.getMinecraft().getResourceManager().getResource(identifier).getInputStream();
        }
    }
}
