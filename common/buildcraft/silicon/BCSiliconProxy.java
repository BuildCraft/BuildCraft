/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import buildcraft.silicon.client.render.RenderLaser;
import buildcraft.silicon.client.render.RenderProgrammingTable;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import buildcraft.silicon.container.ContainerAssemblyTable;
import buildcraft.silicon.container.ContainerIntegrationTable;
import buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import buildcraft.silicon.gui.GuiIntegrationTable;
import buildcraft.silicon.tile.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BCSiliconProxy implements IGuiHandler {
    @SidedProxy(modId = BCSilicon.MODID)
    private static BCSiliconProxy proxy;

    public static BCSiliconProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == BCSiliconGuis.ASSEMBLY_TABLE.ordinal()) {
            if (tile instanceof TileAssemblyTable) {
                TileAssemblyTable assemblyTable = (TileAssemblyTable) tile;
                return new ContainerAssemblyTable(player, assemblyTable);
            }
        }
        if (ID == BCSiliconGuis.ADVANCED_CRAFTING_TABLE.ordinal()) {
            if (tile instanceof TileAdvancedCraftingTable) {
                TileAdvancedCraftingTable advancedCraftingTable = (TileAdvancedCraftingTable) tile;
                return new ContainerAdvancedCraftingTable(player, advancedCraftingTable);
            }
        }
        if (ID == BCSiliconGuis.INTEGRATION_TABLE.ordinal()) {
            if (tile instanceof TileIntegrationTable) {
                TileIntegrationTable integrationTable = (TileIntegrationTable) tile;
                return new ContainerIntegrationTable(player, integrationTable);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlPreInit() {}

    public void fmlInit() {

    }

    public void fmlPostInit() {}

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCSiliconProxy {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCSiliconProxy {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (ID == BCSiliconGuis.ASSEMBLY_TABLE.ordinal()) {
                if (tile instanceof TileAssemblyTable) {
                    TileAssemblyTable assemblyTable = (TileAssemblyTable) tile;
                    return new GuiAssemblyTable(new ContainerAssemblyTable(player, assemblyTable));
                }
            }
            if (ID == BCSiliconGuis.ADVANCED_CRAFTING_TABLE.ordinal()) {
                if (tile instanceof TileAdvancedCraftingTable) {
                    TileAdvancedCraftingTable advancedCraftingTable = (TileAdvancedCraftingTable) tile;
                    return new GuiAdvancedCraftingTable(new ContainerAdvancedCraftingTable(player, advancedCraftingTable));
                }
            }
            if (ID == BCSiliconGuis.INTEGRATION_TABLE.ordinal()) {
                if (tile instanceof TileIntegrationTable) {
                    TileIntegrationTable integrationTable = (TileIntegrationTable) tile;
                    return new GuiIntegrationTable(new ContainerIntegrationTable(player, integrationTable));
                }
            }
            return null;
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, new RenderLaser());
            ClientRegistry.bindTileEntitySpecialRenderer(TileProgrammingTable_Neptune.class, new RenderProgrammingTable());
        }
    }
}
