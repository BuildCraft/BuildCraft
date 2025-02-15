/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.api.BCModules;
import buildcraft.lib.net.MessageManager;
import buildcraft.robotics.zone.MessageZoneMapRequest;
import buildcraft.robotics.zone.MessageZoneMapResponse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;

//public abstract class BCRoboticsProxy implements IGuiHandler
public abstract class BCRoboticsProxy {
    // @SidedProxy(modId = BCRobotics.MODID)
    private static BCRoboticsProxy proxy;

    public static BCRoboticsProxy getProxy() {
        if (proxy == null) {
            switch (FMLLoader.getDist()) {
                case CLIENT:
                    proxy = new BCRoboticsProxy.ClientProxy();
                    break;
                case DEDICATED_SERVER:
                    proxy = new BCRoboticsProxy.ServerProxy();
                    break;
            }
        }
        return proxy;
    }

//    @Override
//    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
//        if (ID == RoboticsGuis.ZONE_PLANTER.ordinal()) {
//            if (tile instanceof TileZonePlanner) {
//                TileZonePlanner zonePlanner = (TileZonePlanner) tile;
//                return new ContainerZonePlanner(player, zonePlanner);
//            }
//        }
//        return null;
//    }

//    @Override
//    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//        return null;
//    }

    public void fmlPreInit() {
        MessageManager.registerMessageClass(BCModules.ROBOTICS, MessageZoneMapRequest.class, MessageZoneMapRequest.HANDLER, Dist.DEDICATED_SERVER);
        MessageManager.registerMessageClass(BCModules.ROBOTICS, MessageZoneMapResponse.class, Dist.CLIENT);
    }

    public void fmlInit() {
    }

    public void fmlPostInit() {
    }

    @SuppressWarnings("unused")
//    @OnlyIn(Dist.DEDICATED_SERVER)
    public static class ServerProxy extends BCRoboticsProxy {
    }

    @SuppressWarnings("unused")
//    @OnlyIn(Dist.CLIENT)
    public static class ClientProxy extends BCRoboticsProxy {
//        @Override
//        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
//            if (ID == RoboticsGuis.ZONE_PLANTER.ordinal()) {
//                if (tile instanceof TileZonePlanner) {
//                    TileZonePlanner zonePlanner = (TileZonePlanner) tile;
//                    return new GuiZonePlanner(new ContainerZonePlanner(player, zonePlanner));
//                }
//            }
//            return null;
//        }

        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            MessageManager.setHandler(MessageZoneMapResponse.class, MessageZoneMapResponse.HANDLER, Dist.CLIENT);
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            // 1.18.2: moved to BCRobotics#onTesrReg
//            ClientRegistry.bindTileEntitySpecialRenderer(TileZonePlanner.class, new RenderZonePlanner());
        }
    }
}
