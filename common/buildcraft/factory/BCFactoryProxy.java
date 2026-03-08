/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import buildcraft.factory.client.render.RenderMiningWell;
import buildcraft.factory.client.render.RenderPump;
import net.minecraftforge.fml.loading.FMLLoader;

//public abstract class BCFactoryProxy implements IGuiHandler
public abstract class BCFactoryProxy {
    // @SidedProxy(modId = BCFactory.MODID)
    private static BCFactoryProxy proxy;

    public static BCFactoryProxy getProxy() {
        if (proxy == null) {
            switch (FMLLoader.getDist()) {
                case CLIENT:
                    proxy = new BCFactoryProxy.ClientProxy();
                    break;
                case DEDICATED_SERVER:
                    proxy = new BCFactoryProxy.ServerProxy();
                    break;
            }
        }
        return proxy;
    }

//    @Override
//    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
//        if (ID == BCFactoryGuis.AUTO_WORKBENCH_ITEMS.ordinal()) {
//            if (tile instanceof TileAutoWorkbenchItems) {
//                TileAutoWorkbenchItems workbench = (TileAutoWorkbenchItems) tile;
//                return new ContainerAutoCraftItems(player, workbench);
//            }
//        }
//        if (ID == BCFactoryGuis.CHUTE.ordinal()) {
//            if (tile instanceof TileChute) {
//                TileChute chute = (TileChute) tile;
//                return new ContainerChute(player, chute);
//            }
//        }
//        return null;
//    }

//    @Override
//    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//        return null;
//    }

    public void fmlPreInit() {
    }

    public void fmlInit() {
    }

    public void fmlPostInit() {
    }

    @SuppressWarnings("unused")
//    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCFactoryProxy {
    }

    @SuppressWarnings("unused")
//    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCFactoryProxy {
//        @Override
//        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
//            if (ID == BCFactoryGuis.AUTO_WORKBENCH_ITEMS.ordinal()) {
//                if (tile instanceof TileAutoWorkbenchItems) {
//                    TileAutoWorkbenchItems workbench = (TileAutoWorkbenchItems) tile;
//                    return new GuiAutoCraftItems(new ContainerAutoCraftItems(player, workbench));
//                }
//            }
//            if (ID == BCFactoryGuis.CHUTE.ordinal()) {
//                if (tile instanceof TileChute) {
//                    TileChute chute = (TileChute) tile;
//                    return new GuiChute(new ContainerChute(player, chute));
//                }
//            }
//            return null;
//        }

        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            RenderPump.init();
            RenderMiningWell.init();
            BCFactoryModels.fmlPreInit();
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            BCFactoryModels.fmlInit();
        }
    }
}
