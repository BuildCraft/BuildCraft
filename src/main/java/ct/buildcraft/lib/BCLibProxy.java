/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package ct.buildcraft.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import ct.buildcraft.api.BCModules;
import ct.buildcraft.lib.net.MessageContainer;
import ct.buildcraft.lib.net.MessageDebugRequest;
import ct.buildcraft.lib.net.MessageDebugResponse;
import ct.buildcraft.lib.net.MessageManager;
import ct.buildcraft.lib.net.MessageMarker;
import ct.buildcraft.lib.net.MessageUpdateTile;
import ct.buildcraft.lib.net.cache.MessageObjectCacheRequest;
import ct.buildcraft.lib.net.cache.MessageObjectCacheResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public abstract class BCLibProxy {

    static void MessageRegistry() {
    	MessageManager.registerMessageClass(BCModules.LIB, MessageUpdateTile.class, MessageUpdateTile.HANDLER, MessageUpdateTile::toBytes, MessageUpdateTile::new);
        MessageManager.registerMessageClass(BCModules.LIB, MessageContainer.class, MessageContainer.HANDLER, MessageContainer::toBytes, MessageContainer::new);
        MessageManager.registerMessageClass(BCModules.LIB, MessageMarker.class, MessageMarker.HANDLER, MessageMarker::toBytes, MessageMarker::new/*, Dist.CLIENT*/);
        MessageManager.registerMessageClass(BCModules.LIB, MessageObjectCacheRequest.class,
        MessageObjectCacheRequest.HANDLER, MessageObjectCacheRequest::toBytes, MessageObjectCacheRequest::new/*, Dist.DEDICATED_SERVER*/);
        MessageManager.registerMessageClass(BCModules.LIB, MessageObjectCacheResponse.class, MessageObjectCacheResponse.HANDLER, MessageObjectCacheResponse::toBytes, MessageObjectCacheResponse::new/*, Dist.CLIENT*/);
        MessageManager.registerMessageClass(BCModules.LIB, MessageDebugRequest.class, MessageDebugRequest.HANDLER, MessageDebugRequest::toBytes, MessageDebugRequest::new, 
            Dist.DEDICATED_SERVER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageDebugResponse.class, MessageDebugResponse.HANDLER, MessageDebugResponse::toBytes, MessageDebugResponse::new/*, Dist.CLIENT*/);
    }

    static void fmlInit() {}

    void fmlPostInit() {}

    public Level getClientLevel() {
        return null;
    }

    public static Player getClientPlayer() {
    	Player[] player = {null};
    	DistExecutor.safeRunWhenOn(Dist.CLIENT, ()-> ()->{
    		var mc = Minecraft.getInstance();
    		player[0] = mc.player;
    	});
        return player[0];
    }


    public <T extends BlockEntity> T getServerTile(T tile) {
        return tile;
    }

    public InputStream getStreamForIdentifier(ResourceLocation identifier) throws IOException {
        return null;
    }

    public abstract File getGameDirectory();

    public Iterable<File> getLoadedResourcePackFiles() {
        return Collections.emptySet();
    }
    

/*
    @SuppressWarnings("unused")
    @OnlyIn(Dist.CLIENT)
    public static class ClientProxy extends BCLibProxy {
        void MessageRegistry() {
            super.MessageRegistry();

/*            ReloadableRegistryManager manager = ReloadableRegistryManager.RESOURCE_PACKS;
            BuildCraftRegistryManager.managerResourcePacks = manager;
            manager.registerRegistry(GuidePageRegistry.INSTANCE);

            DetachedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_Level_ORIGIN, MarkerRenderer.INSTANCE);
            DetachedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_Level_ORIGIN, DebugRenderHelper.INSTANCE);
            // various sprite registers
            BCLibSprites.fmlPreInitClient();
            BCLibConfig.configChangeListeners.add(LibConfigChangeListener.INSTANCE);

            MessageManager.setHandler(MessageMarker.class, MessageMarker.HANDLER, Dist.CLIENT);
            MessageManager.setHandler(MessageObjectCacheResponse.class, MessageObjectCacheResponse.HANDLER,
                Dist.CLIENT);
            MessageManager.setHandler(MessageDebugResponse.class, MessageDebugResponse.HANDLER, Dist.CLIENT);*/
/*        }

        @Override
        void fmlInit() {
            super.fmlInit();
        }

        @Override
        void fmlPostInit() {
            super.fmlPostInit();
/*            if (BCLibItems.isGuideEnabled()) {
                ResourceManager manager = Minecraft.getInstance().getResourceManager();
                ReloadableResourceManager reloadable = (ReloadableResourceManager) manager;
                reloadable.registerReloadListener(GuideManager.INSTANCE);
            }
            GuiConfigManager.loadFromConfigFile();*/
/*        }

        @Override
        public Level getClientLevel() {
            return Minecraft.getInstance().level;
        }



/*        @Override
        public void addScheduledTask(Level Level, Runnable task) {
            if (Level instanceof LevelClient) {
                Minecraft.getInstance().tell(task));
            } else {
                super.addScheduledTask(Level, task);
            }
        }*/

/*        @SuppressWarnings("unchecked")
        @Override
        public <T extends BlockEntity> T getServerTile(T tile) {
            if (tile != null && tile.hasLevel()) {
                Level level = tile.getLevel();
                if (level.isClientSide && Minecraft.getInstance().isLocalServer()) {
                    ServerLevel server = ServerLifecycleHooks.getCurrentServer().getLevel(level.dimension());
                    if (server == null) return tile;
                    BlockEntity atServer = server.getBlockEntity(tile.getBlockPos());
                    if (atServer == null) return tile;
                    if (atServer.getClass() == tile.getClass()) {
                        return (T) atServer;
                    }
                }
            }
            return tile;
        }

        @SuppressWarnings("resource")
		@Override
        public File getGameDirectory() {
            return Minecraft.getInstance().gameDirectory;
        }

        @Override
        public Iterable<File> getLoadedResourcePackFiles() {
            List<File> files = new ArrayList<>();
            for (Pack entry : Minecraft.getInstance().getResourcePackRepository().getAvailablePacks()) {
            	PackResources pack = entry.open();
                if (pack instanceof AbstractPackResources) {
                    AbstractPackResources p = (AbstractPackResources) pack;
                    Object f = null;
                    for(Object g : AbstractPackResources.class.getDeclaredFields()) {
                    	if(g instanceof File) 
                    		f = g;
                    }
                    if (f == null) {
                        throw new Error("We've got the wrong field! (Expected a file but got " + "null" + ")");
                    }
                    files.add((File) f);
                }
            }
            return files;
        }


        @Override
        public InputStream getStreamForIdentifier(ResourceLocation identifier) throws IOException {
            return Minecraft.getInstance().getResourceManager().open(identifier);
        }
    }*/
}
