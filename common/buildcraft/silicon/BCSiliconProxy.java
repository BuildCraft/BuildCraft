/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.misc.MessageUtil;

import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import buildcraft.silicon.container.ContainerAssemblyTable;
import buildcraft.silicon.container.ContainerGate;
import buildcraft.silicon.container.ContainerIntegrationTable;
import buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import buildcraft.silicon.gui.GuiGate;
import buildcraft.silicon.gui.GuiIntegrationTable;
import buildcraft.silicon.plug.PluggableGate;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileIntegrationTable;

public abstract class BCSiliconProxy implements IGuiHandler {
    @SidedProxy(modId = BCSilicon.MODID)
    private static BCSiliconProxy proxy;

    public static BCSiliconProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        int data = id >>> 8;
        id = id & 0xFF;
        if (id == BCSiliconGuis.ASSEMBLY_TABLE.ordinal()) {
            if (tile instanceof TileAssemblyTable) {
                TileAssemblyTable assemblyTable = (TileAssemblyTable) tile;
                return new ContainerAssemblyTable(player, assemblyTable);
            }
        }
        if (id == BCSiliconGuis.ADVANCED_CRAFTING_TABLE.ordinal()) {
            if (tile instanceof TileAdvancedCraftingTable) {
                TileAdvancedCraftingTable advancedCraftingTable = (TileAdvancedCraftingTable) tile;
                return new ContainerAdvancedCraftingTable(player, advancedCraftingTable);
            }
        }
        if (id == BCSiliconGuis.INTEGRATION_TABLE.ordinal()) {
            if (tile instanceof TileIntegrationTable) {
                TileIntegrationTable integrationTable = (TileIntegrationTable) tile;
                return new ContainerIntegrationTable(player, integrationTable);
            }
        }
        if (id == BCSiliconGuis.GATE.ordinal()) {
            EnumFacing gateSide = EnumFacing.getFront(data);
            if (tile instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) tile;
                PipePluggable plug = holder.getPluggable(gateSide);
                if (plug instanceof PluggableGate) {
                    ContainerGate container = new ContainerGate(player, ((PluggableGate) plug).logic);
                    MessageUtil.doDelayedServer(() -> {
                        container.sendMessage(ContainerGate.ID_VALID_STATEMENTS);
                    });
                    return container;
                }
            }
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

    @SuppressWarnings("unused")
    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCSiliconProxy {}

    @SuppressWarnings("unused")
    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCSiliconProxy {

        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            BCSiliconSprites.fmlPreInit();
            BCSiliconModels.fmlPreInit();
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            BCSiliconModels.fmlInit();
        }

        @Override
        public void fmlPostInit() {
            super.fmlPostInit();
            BCSiliconModels.fmlPostInit();
        }

        @Override
        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            int data = id >>> 8;
            id = id & 0xFF;
            if (id == BCSiliconGuis.ASSEMBLY_TABLE.ordinal()) {
                if (tile instanceof TileAssemblyTable) {
                    TileAssemblyTable assemblyTable = (TileAssemblyTable) tile;
                    return new GuiAssemblyTable(new ContainerAssemblyTable(player, assemblyTable));
                }
            }
            if (id == BCSiliconGuis.ADVANCED_CRAFTING_TABLE.ordinal()) {
                if (tile instanceof TileAdvancedCraftingTable) {
                    TileAdvancedCraftingTable advancedCraftingTable = (TileAdvancedCraftingTable) tile;
                    return new GuiAdvancedCraftingTable(
                        new ContainerAdvancedCraftingTable(player, advancedCraftingTable));
                }
            }
            if (id == BCSiliconGuis.INTEGRATION_TABLE.ordinal()) {
                if (tile instanceof TileIntegrationTable) {
                    TileIntegrationTable integrationTable = (TileIntegrationTable) tile;
                    return new GuiIntegrationTable(new ContainerIntegrationTable(player, integrationTable));
                }
            }
            if (id == BCSiliconGuis.GATE.ordinal()) {
                EnumFacing gateSide = EnumFacing.getFront(data);
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    PipePluggable plug = holder.getPluggable(gateSide);
                    if (plug instanceof PluggableGate) {
                        return new GuiGate(new ContainerGate(player, ((PluggableGate) plug).logic));
                    }
                }
            }
            return null;
        }
    }
}
