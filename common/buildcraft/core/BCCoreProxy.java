/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.BCModules;

import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.DetachedRenderer.RenderMatrixType;
import buildcraft.lib.net.MessageManager;

import buildcraft.core.client.RenderTickListener;
import buildcraft.core.client.render.RenderVolumeBoxes;
import buildcraft.core.list.ContainerList;
import buildcraft.core.list.GuiList;
import buildcraft.core.list.ListTooltipHandler;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public abstract class BCCoreProxy implements IGuiHandler {
    @SidedProxy(modId = BCCore.MODID)
    private static BCCoreProxy proxy = null;

    public static BCCoreProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
        if (ID == BCCoreGuis.LIST.ordinal()) {
            return new ContainerList(player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlPreInit() {
        MessageManager.registerMessageClass(BCModules.CORE, MessageVolumeBoxes.class, EnvType.CLIENT);
    }

    public void fmlInit() {}

    public void fmlPostInit() {}

    public List<VolumeBox> getVolumeBoxes(World world) {
        return WorldSavedDataVolumeBoxes.get(world).volumeBoxes;
    }

    @Environment(EnvType.SERVER)
    public static class ServerProxy extends BCCoreProxy {

    }

    @Environment(EnvType.CLIENT)
    public static class ClientProxy extends BCCoreProxy {
        @Override
        public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
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
            DetachedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, RenderVolumeBoxes.INSTANCE);
            MinecraftForge.EVENT_BUS.register(ListTooltipHandler.INSTANCE);
            MessageManager.setHandler(MessageVolumeBoxes.class, MessageVolumeBoxes.HANDLER, EnvType.CLIENT);
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            BCCoreModels.fmlInit();
            MinecraftForge.EVENT_BUS.register(RenderTickListener.class);
        }

        @Override
        public List<VolumeBox> getVolumeBoxes(World world) {
            return world.isClient ? ClientVolumeBoxes.INSTANCE.volumeBoxes : super.getVolumeBoxes(world);
        }
    }
}
