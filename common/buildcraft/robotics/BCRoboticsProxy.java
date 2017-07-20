/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.robotics.client.render.RenderZonePlanner;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.gui.GuiZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    public void fmlInit() {}

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCRoboticsProxy {

    }

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
        public void fmlInit() {
            super.fmlInit();
            ClientRegistry.bindTileEntitySpecialRenderer(TileZonePlanner.class, new RenderZonePlanner());
        }
    }
}
