/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.factory.client.render.RenderMiningWell;
import buildcraft.factory.client.render.RenderPump;
import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.factory.container.ContainerChute;
import buildcraft.factory.container.ContainerDistiller;
import buildcraft.factory.container.ContainerTank;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.gui.GuiChute;
import buildcraft.factory.gui.GuiDistiller;
import buildcraft.factory.gui.GuiTank;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.factory.tile.TileTank;

public abstract class BCFactoryProxy implements IGuiHandler {
    @SidedProxy(modId = BCFactory.MODID)
    private static BCFactoryProxy proxy;

    public static BCFactoryProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
        BlockEntity tile = world.getBlockEntity(new BlockPos(x, y, z));
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
        if (ID == BCFactoryGuis.TANK.ordinal()) {
            if (tile instanceof TileTank) {
                return new ContainerTank(player, (TileTank) tile);
            }
        }
        if (ID == BCFactoryGuis.DISTILLER.ordinal()) {
            if (tile instanceof TileDistiller_BC8) {
                return new ContainerDistiller(player, (TileDistiller_BC8) tile);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlPreInit() {
    }

    public void fmlInit() {
    }

    public void fmlPostInit() {
    }

    @SuppressWarnings("unused")
    @Environment(EnvType.SERVER)
    public static class ServerProxy extends BCFactoryProxy {
    }

    @SuppressWarnings("unused")
    @Environment(EnvType.CLIENT)
    public static class ClientProxy extends BCFactoryProxy {
        @Override
        public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
            BlockEntity tile = world.getBlockEntity(new BlockPos(x, y, z));
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
            if (ID == BCFactoryGuis.TANK.ordinal()) {
                if (tile instanceof TileTank) {
                    return new GuiTank(new ContainerTank(player, (TileTank) tile));
                }
            }
            if (ID == BCFactoryGuis.DISTILLER.ordinal()) {
                if (tile instanceof TileDistiller_BC8) {
                    return new GuiDistiller(new ContainerDistiller(player, (TileDistiller_BC8) tile));
                }
            }
            return null;
        }

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
