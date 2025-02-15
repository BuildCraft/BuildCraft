/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.energy.event.ChristmasHandler;
import net.minecraftforge.fml.loading.FMLLoader;

//public abstract class BCEnergyProxy implements IGuiHandler
public abstract class BCEnergyProxy {
    // @SidedProxy(modId = BCEnergy.MODID)
    private static BCEnergyProxy proxy;

    public static BCEnergyProxy getProxy() {
        if (proxy == null) {
            switch (FMLLoader.getDist()) {
                case CLIENT:
                    proxy = new BCEnergyProxy.ClientProxy();
                    break;
                case DEDICATED_SERVER:
                    proxy = new BCEnergyProxy.ServerProxy();
                    break;
            }
        }
        return proxy;
    }

    public void fmlPreInit() {
    }

    public void fmlInit() {
    }

    public void fmlPostInit() {
    }

//    @Override
//    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
//        return null;
//    }

//    @Override
//    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
//        BCEnergyGuis gui = BCEnergyGuis.get(id);
//        if (gui == null) return null;
//        BlockPos pos = new BlockPos(x, y, z);
//        TileEntity tile = world.getTileEntity(pos);
//        switch (gui) {
//            case ENGINE_STONE:
//                if (tile instanceof TileEngineStone_BC8) {
//                    return new ContainerEngineStone_BC8(player, (TileEngineStone_BC8) tile);
//                }
//                return null;
//            case ENGINE_IRON:
//                if (tile instanceof TileEngineIron_BC8) {
//                    return new ContainerEngineIron_BC8(player, (TileEngineIron_BC8) tile);
//                }
//
//                return null;
//            default:
//                return null;
//        }
//    }

    // @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCEnergyProxy {
        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            ChristmasHandler.fmlPreInitDedicatedServer();
        }
    }

    // @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCEnergyProxy {
        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            ChristmasHandler.fmlPreInitClient();
            BCEnergyModels.fmlPreInit();
            BCEnergySprites.fmlPreInit();
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            // moved to BCEnergyModels#onTesrReg
//            ClientRegistry.bindTileEntitySpecialRenderer(TileEngineStone_BC8.class, RenderEngineStone.INSTANCE);
//            ClientRegistry.bindTileEntitySpecialRenderer(TileEngineIron_BC8.class, RenderEngineIron.INSTANCE);
        }

//        @Override
//        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
//            BCEnergyGuis gui = BCEnergyGuis.get(id);
//            if (gui == null) return null;
//            BlockPos pos = new BlockPos(x, y, z);
//            TileEntity tile = world.getTileEntity(pos);
//            switch (gui) {
//                case ENGINE_STONE:
//                    if (tile instanceof TileEngineStone_BC8) {
//                        return new GuiEngineStone_BC8(new ContainerEngineStone_BC8(player, (TileEngineStone_BC8) tile));
//                    }
//                    return null;
//                case ENGINE_IRON:
//                    if (tile instanceof TileEngineIron_BC8) {
//                        return new GuiEngineIron_BC8(new ContainerEngineIron_BC8(player, (TileEngineIron_BC8) tile));
//                    }
//                    return null;
//                default:
//                    return null;
//            }
//        }
    }
}
