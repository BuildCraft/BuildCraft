/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.DetachedRenderer.RenderMatrixType;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.client.RenderTickListener;
import buildcraft.core.client.render.RenderVolumeInWorld;
import buildcraft.core.list.ContainerList;
import buildcraft.core.list.GuiList;
import buildcraft.core.list.ListTooltipHandler;

public abstract class BCCoreProxy implements IGuiHandler {
    @SidedProxy(modId = BCCore.MODID)
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
            BCCoreModels.fmlPreInit();
            BuildCraftLaserManager.fmlPreInit();
            DetachedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, RenderVolumeInWorld.INSTANCE);
            MinecraftForge.EVENT_BUS.register(ListTooltipHandler.INSTANCE);
        }

        @Override
        public void fmlInit() {
            BCCoreModels.fmlInit();
            MinecraftForge.EVENT_BUS.register(RenderTickListener.class);
        }
    }
}
