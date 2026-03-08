/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import buildcraft.api.BCModules;
import buildcraft.core.client.RenderTickListener;
import buildcraft.core.client.render.RenderVolumeBoxes;
import buildcraft.core.list.ListTooltipHandler;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.net.MessageManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.List;

//public abstract class BCCoreProxy implements IGuiHandler
public abstract class BCCoreProxy {
    // @SidedProxy(modId = BCCore.MODID)
    private static BCCoreProxy proxy = null;

    public static BCCoreProxy getProxy() {
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

//    @Override
//    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//        if (ID == BCCoreGuis.LIST.ordinal()) {
//            return new ContainerList(player);
//        }
//        return null;
//    }

//    @Override
//    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//        return null;
//    }

    public void fmlPreInit() {
        MessageManager.registerMessageClass(BCModules.CORE, MessageVolumeBoxes.class, Dist.CLIENT);
    }

    public void fmlInit() {
    }

    public void fmlPostInit() {
    }

    public List<VolumeBox> getVolumeBoxes(Level world) {
        return WorldSavedDataVolumeBoxes.get(world).volumeBoxes;
    }

    // @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCCoreProxy {

    }

    // @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCCoreProxy {
//        @Override
//        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//            if (ID == BCCoreGuis.LIST.ordinal()) {
//                return new GuiList(player);
//            }
//            return null;
//        }

        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            BCCoreSprites.fmlPreInit();
            BCCoreModels.fmlPreInit();
            DetachedRenderer.INSTANCE.addRenderer(DetachedRenderer.RenderMatrixType.FROM_WORLD_ORIGIN, RenderVolumeBoxes.INSTANCE);
            MinecraftForge.EVENT_BUS.register(ListTooltipHandler.INSTANCE);
            MessageManager.setHandler(MessageVolumeBoxes.class, MessageVolumeBoxes.HANDLER, Dist.CLIENT);
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
//            BCCoreModels.fmlInit(); // 1.18.2: TESR reg use forge event
            MinecraftForge.EVENT_BUS.register(RenderTickListener.class);
        }

        @Override
        public List<VolumeBox> getVolumeBoxes(Level world) {
            return world.isClientSide ? ClientVolumeBoxes.INSTANCE.volumeBoxes : super.getVolumeBoxes(world);
        }
    }
}
