/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.client.RenderTickListener;
import buildcraft.core.client.render.RenderMarkerVolume;
import buildcraft.core.client.render.RenderVolumeInWorld;
import buildcraft.core.list.ContainerList;
import buildcraft.core.list.GuiList;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.client.render.DetatchedRenderer;
import buildcraft.lib.client.render.DetatchedRenderer.RenderMatrixType;

public abstract class BCCoreProxy implements IGuiHandler {
    @SidedProxy
    private static BCCoreProxy proxy = null;

    public static BCCoreProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == BCCoreGuis.LIST.ordinal()) {
            return new ContainerList(player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlPreInit() {}

    public void fmlInit() {}

    public void fmlPostInit() {}

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCCoreProxy {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCCoreProxy {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            if (ID == BCCoreGuis.LIST.ordinal()) {
                return new GuiList(player);
            }
            return null;
        }

        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            BCCoreSprites.fmlPreInit();
            BuildCraftLaserManager.fmlPreInit();
            DetatchedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, RenderVolumeInWorld.INSTANCE);
        }

        @Override
        public void fmlInit() {
            ClientRegistry.bindTileEntitySpecialRenderer(TileMarkerVolume.class, RenderMarkerVolume.INSTANCE);

            MinecraftForge.EVENT_BUS.register(RenderTickListener.INSTANCE);
        }
    }
}
