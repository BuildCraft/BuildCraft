/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.BCModules;

import buildcraft.lib.net.MessageManager;

import buildcraft.robotics.client.render.RenderZonePlanner;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.gui.GuiZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.MessageZoneMapRequest;
import buildcraft.robotics.zone.MessageZoneMapResponse;

public abstract class BCRoboticsProxy implements IGuiHandler {
    @SidedProxy(modId = BCRobotics.MODID)
    private static BCRoboticsProxy proxy;

    public static BCRoboticsProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == RoboticsGuis.ZONE_PLANTER.ordinal()) {
            if (tile instanceof TileZonePlanner) {
                TileZonePlanner zonePlanner = (TileZonePlanner) tile;
                return new ContainerZonePlanner(player, zonePlanner);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlPreInit() {
        MessageManager.registerMessageClass(BCModules.ROBOTICS, MessageZoneMapRequest.class, MessageZoneMapRequest.HANDLER, Side.SERVER);
        MessageManager.registerMessageClass(BCModules.ROBOTICS, MessageZoneMapResponse.class, Side.CLIENT);
    }

    public void fmlInit() {
    }

    public void fmlPostInit() {
    }

    @SuppressWarnings("unused")
    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCRoboticsProxy {
    }

    @SuppressWarnings("unused")
    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCRoboticsProxy {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (ID == RoboticsGuis.ZONE_PLANTER.ordinal()) {
                if (tile instanceof TileZonePlanner) {
                    TileZonePlanner zonePlanner = (TileZonePlanner) tile;
                    return new GuiZonePlanner(new ContainerZonePlanner(player, zonePlanner));
                }
            }
            return null;
        }

        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            MessageManager.setHandler(MessageZoneMapResponse.class, MessageZoneMapResponse.HANDLER, Side.CLIENT);
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            ClientRegistry.bindTileEntitySpecialRenderer(TileZonePlanner.class, new RenderZonePlanner());
        }
    }
}
