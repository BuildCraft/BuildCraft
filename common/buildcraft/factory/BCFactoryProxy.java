/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.factory.client.render.RenderMiningWell;
import buildcraft.factory.client.render.RenderPump;
import buildcraft.factory.client.render.RenderTank;
import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.factory.container.ContainerChute;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.gui.GuiChute;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileTank;

public abstract class BCFactoryProxy implements IGuiHandler {
    @SidedProxy
    private static BCFactoryProxy proxy;

    public static BCFactoryProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == BCFactoryGuis.AUTO_WORKBENCH_ITEMS.ordinal()) {
            if (tile instanceof TileAutoWorkbenchItems) {
                TileAutoWorkbenchItems workbench = (TileAutoWorkbenchItems) tile;
                return new ContainerAutoCraftItems(player, workbench);
            }
        }
        if (ID == BCFactoryGuis.CHUTE.ordinal()) {
            if (tile instanceof TileChute) {
                TileChute chute = (TileChute) tile;
                return new ContainerChute(player, chute);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlInit() {}

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCFactoryProxy {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCFactoryProxy {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (ID == BCFactoryGuis.AUTO_WORKBENCH_ITEMS.ordinal()) {
                if (tile instanceof TileAutoWorkbenchItems) {
                    TileAutoWorkbenchItems workbench = (TileAutoWorkbenchItems) tile;
                    return new GuiAutoCraftItems(new ContainerAutoCraftItems(player, workbench));
                }
            }
            if (ID == BCFactoryGuis.CHUTE.ordinal()) {
                if (tile instanceof TileChute) {
                    TileChute chute = (TileChute) tile;
                    return new GuiChute(new ContainerChute(player, chute));
                }
            }
            return null;
        }

        @Override
        public void fmlInit() {
            ClientRegistry.bindTileEntitySpecialRenderer(TileMiningWell.class, new RenderMiningWell());
            ClientRegistry.bindTileEntitySpecialRenderer(TilePump.class, new RenderPump());
            ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new RenderTank());
            OBJLoader.INSTANCE.addDomain("buildcraftfactory");
        }
    }
}
