/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.render.DetatchedRenderer;
import buildcraft.lib.client.render.DetatchedRenderer.RenderMatrixType;
import buildcraft.lib.client.render.MarkerRenderer;
import buildcraft.lib.client.resource.ResourceRegistry;
import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.item.ItemManager;

public abstract class LibProxy implements IGuiHandler {
    @SidedProxy
    private static LibProxy proxy;

    public static LibProxy getProxy() {
        return proxy;
    }

    void postRegisterItem(IItemBuildCraft item) {}

    void postRegisterBlock(BlockBCBase_Neptune block) {}

    void fmlPreInit() {}

    void fmlInit() {}

    void fmlPostInit() {}

    public World getClientWorld() {
        return null;
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    public EntityPlayer getPlayerForContext(MessageContext ctx) {
        return ctx.getServerHandler().playerEntity;
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

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends LibProxy {
        @Override
        public void postRegisterItem(IItemBuildCraft item) {
            item.postRegisterClient();
        }

        @Override
        void fmlPreInit() {
            super.fmlPreInit();
            DetatchedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, MarkerRenderer.INSTANCE);
            DetatchedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, BCAdvDebugging.INSTANCE);
            // FIXME TEMP!
            ModelLoaderRegistry.registerLoader(ObjJsonLoader.INSTANCE);
            // various sprite registers
            Ledger_Neptune.fmlPreInitClient();
            LedgerOwnership.fmlPreInitClient();
        }

        @Override
        void fmlInit() {
            super.fmlInit();
            IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
            IReloadableResourceManager reloadable = (IReloadableResourceManager) manager;
            reloadable.registerReloadListener(ResourceRegistry.INSTANCE);
            ItemManager.fmlInitClient();
        }

        @Override
        void fmlPostInit() {
            super.fmlPostInit();
            GuideManager.INSTANCE.load();
        }

        @Override
        public World getClientWorld() {
            return Minecraft.getMinecraft().theWorld;
        }

        @Override
        public EntityPlayer getClientPlayer() {
            return Minecraft.getMinecraft().thePlayer;
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

        @Override
        public <T extends TileEntity> T getServerTile(T tile) {
            if (tile != null && tile.hasWorldObj()) {
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
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            return new GuiGuide();
        }
    }

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends LibProxy {}
}
