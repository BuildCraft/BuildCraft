/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib;

import buildcraft.api.BCModules;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.registry.BuildCraftRegistryManager;
import buildcraft.api.tiles.TilesAPI;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.GuidePageRegistry;
import buildcraft.lib.client.reload.LibConfigChangeListener;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.DetachedRenderer.RenderMatrixType;
import buildcraft.lib.client.render.MarkerRenderer;
import buildcraft.lib.debug.DebugRenderHelper;
import buildcraft.lib.net.*;
import buildcraft.lib.net.cache.MessageObjectCacheRequest;
import buildcraft.lib.net.cache.MessageObjectCacheResponse;
import buildcraft.lib.script.ReloadableRegistryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//public abstract class BCLibProxy implements IGuiHandler
public abstract class BCLibProxy {
    // @SidedProxy(modId = BCLib.MODID)
    private static BCLibProxy proxy;

    public static BCLibProxy getProxy() {
        if (proxy == null) {
            switch (FMLLoader.getDist()) {
                case CLIENT:
                    proxy = new ClientProxy();
                    break;
                case DEDICATED_SERVER:
                    proxy = new ServerProxy();
                    break;
            }
        }
        return proxy;
    }

    void fmlPreInit() {
        MessageManager.registerMessageClass(BCModules.LIB, MessageUpdateTile.class, MessageUpdateTile.HANDLER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageContainer.class, MessageContainer.HANDLER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageMarker.class, Dist.CLIENT);
        MessageManager.registerMessageClass(BCModules.LIB, MessageObjectCacheRequest.class, MessageObjectCacheRequest.HANDLER, Dist.DEDICATED_SERVER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageObjectCacheResponse.class, Dist.CLIENT);
        MessageManager.registerMessageClass(BCModules.LIB, MessageDebugRequest.class, MessageDebugRequest.HANDLER, Dist.DEDICATED_SERVER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageDebugResponse.class, Dist.CLIENT);
    }

    void fmlInit() {
        MjAPI.regCaps();
        PipeApi.regCaps();
        TilesAPI.regCaps();
    }

    void fmlPostInit() {
    }

    public World getClientWorld() {
        return null;
    }

    public PlayerEntity getClientPlayer() {
        return null;
    }

    public PlayerEntity getPlayerForContext(NetworkEvent.Context ctx) {
        return ctx.getSender();
    }

    public void addScheduledTask(World world, Runnable task) {
        if (world instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) world;
            server.getServer().execute(task);
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

//    @Override
//    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//        return null;
//    }

//    @Override
//    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//        return null;
//    }

    @SuppressWarnings("unused")
//    @OnlyIn(Dist.DEDICATED_SERVER)
    public static class ServerProxy extends BCLibProxy {
        @Override
        public File getGameDirectory() {
            return FMLPaths.GAMEDIR.get().toFile();
        }
    }

    @SuppressWarnings("unused")
//    @OnlyIn(Dist.CLIENT)
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

            MessageManager.setHandler(MessageMarker.class, MessageMarker.HANDLER, Dist.CLIENT);
            MessageManager.setHandler(MessageObjectCacheResponse.class, MessageObjectCacheResponse.HANDLER, Dist.CLIENT);
            MessageManager.setHandler(MessageDebugResponse.class, MessageDebugResponse.HANDLER, Dist.CLIENT);
        }

        @Override
        void fmlInit() {
            super.fmlInit();
        }

        @Override
        void fmlPostInit() {
            super.fmlPostInit();
            if (BCLibItems.isGuideEnabled()) {
                IResourceManager manager = Minecraft.getInstance().getResourceManager();
//                IReloadableResourceManager reloadable = (IReloadableResourceManager) manager;
                IReloadableResourceManager reloadable = (IReloadableResourceManager) manager;
//                reloadable.registerReloadListener(GuideManager.INSTANCE);
                this.registerReloadListener(reloadable, GuideManager.INSTANCE);
            }
//            GuiConfigManager.loadFromConfigFile(); // Calen: moved to BCLibEventDist#reload
        }

        // Calen: ReloadableResourceManager#registerReloadListener in 1.18.2 lacks something, here is what there should be in 1.12.2
        private void registerReloadListener(IReloadableResourceManager reloadable, IResourceManagerReloadListener reloadListener) {
            /* Calen: Don't call reloadable.registerReloadListener(reloadListener).
             * Load everything at RecipesUpdatedEvent.
             * When switching language before joining a world, Minecraft.getInstance().level is null, and recipes will not be loaded. */
//            reloadable.registerReloadListener(reloadListener);
            // Calen: moved to BCLibEventDistForgeBus#onTextureStitchPost
//            reloadListener.onResourceManagerReload(reloadable);
            BCLibEventDist.INSTANCE.addReloadListeners(reloadListener);
        }

        @Override
        public World getClientWorld() {
            return Minecraft.getInstance().level;
        }

        @Override
        public PlayerEntity getClientPlayer() {
            return Minecraft.getInstance().player;
        }

        @Override
        public PlayerEntity getPlayerForContext(NetworkEvent.Context ctx) {
            if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                return super.getPlayerForContext(ctx);
            }
            return getClientPlayer();
        }

        @Override
        public void addScheduledTask(World world, Runnable task) {
            if (world instanceof ClientWorld) {
                Minecraft.getInstance().execute(task);
            } else {
                super.addScheduledTask(world, task);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends TileEntity> T getServerTile(T tile) {
            if (tile != null && tile.hasLevel()) {
                World world = tile.getLevel();
                if (world.isClientSide && Minecraft.getInstance().hasSingleplayerServer()) {
//                    ServerWorld server = DimensionManager.getWorld(world.provider.getDimension());
                    ServerWorld server = world.getServer().getLevel(world.dimension());
                    if (server == null) return tile;
                    TileEntity atServer = server.getBlockEntity(tile.getBlockPos());
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
            return Minecraft.getInstance().gameDirectory;
        }

        @Override
        public Iterable<File> getLoadedResourcePackFiles() {
            List<File> files = new ArrayList<>();
//            for (ResourcePackRepository.Entry entry : Minecraft.getInstance().getResourcePackRepository().getRepositoryEntries())
            for (ResourcePackInfo pack : Minecraft.getInstance().getResourcePackRepository().getAvailablePacks()) {
//                IResourcePack pack = entry.getResourcePack();
//                if (pack instanceof AbstractResourcePack)
                IResourcePack opened = pack.open();
                if (opened instanceof ResourcePack) {
//                    AbstractResourcePack p = (AbstractResourcePack) pack;
                    ResourcePack p = (ResourcePack) opened;
//                    Object f = ObfuscationReflectionHelper.getPrivateValue(AbstractResourcePack.class, p, 1);
//                    if (!(f instanceof File)) {
//                        throw new Error("We've got the wrong field! (Expected a file but got " + f + ")");
//                    }
//                    files.add((File) f);
                    if (p.file.exists()) {
                        files.add(p.file);
                    }
                }
                opened.close();
            }
            return files;
        }

//        @Override
//        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
//            if (id == 0) {
//                EnumHand hand = x == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
//                ItemStack stack = player.getHeldItem(hand);
//                String name = ItemGuide.getBookName(stack);
//                if (name == null) {
//                    return new GuiGuide();
//                } else {
//                    return new GuiGuide(name);
//                }
//            }
//            return null;
//        }

        @Override
        public InputStream getStreamForIdentifier(ResourceLocation identifier) throws IOException {
            return Minecraft.getInstance().getResourceManager().getResource(identifier).getInputStream();
        }
    }
}
