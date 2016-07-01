/* Copyright (c) 2016 AlexIIL and the BuildCraft team
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
import buildcraft.core.list.ContainerList;
import buildcraft.core.list.GuiList;
import buildcraft.core.tile.TileMarkerVolume;

public abstract class CoreProxy implements IGuiHandler {
    @SidedProxy
    private static CoreProxy proxy = null;

    public static CoreProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == CoreGuis.LIST.ordinal()) {
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
    public static class ServerProxy extends CoreProxy {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends CoreProxy {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            if (ID == CoreGuis.LIST.ordinal()) {
                return new GuiList(player);
            }
            return null;
        }

        @Override
        public void fmlPreInit() {
            super.fmlPreInit();

            BuildCraftLaserManager.fmlPreInit();
        }

        @Override
        public void fmlInit() {
            ClientRegistry.bindTileEntitySpecialRenderer(TileMarkerVolume.class, RenderMarkerVolume.INSTANCE);
            // ClientRegistry.bindTileEntitySpecialRenderer(TileMarkerPath.class, RenderMarkerPath.INSTANCE);

            MinecraftForge.EVENT_BUS.register(RenderTickListener.INSTANCE);
        }
    }
}
