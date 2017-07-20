/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.client.render.*;
import buildcraft.builders.container.*;
import buildcraft.builders.gui.*;
import buildcraft.builders.tile.*;
import buildcraft.lib.client.render.DetachedRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BCBuildersProxy implements IGuiHandler {
    @SidedProxy
    private static BCBuildersProxy proxy;

    public static BCBuildersProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (id == BCBuildersGuis.LIBRARY.ordinal()) {
            if (tile instanceof TileElectronicLibrary) {
                TileElectronicLibrary electronicLibrary = (TileElectronicLibrary) tile;
                return new ContainerElectronicLibrary(player, electronicLibrary);
            }
        }
        if (id == BCBuildersGuis.BUILDER.ordinal()) {
            if (tile instanceof TileBuilder) {
                TileBuilder builder = (TileBuilder) tile;
                return new ContainerBuilder(player, builder);
            }
        }
        if (id == BCBuildersGuis.FILLER.ordinal()) {
            if (tile instanceof TileFiller) {
                TileFiller filler = (TileFiller) tile;
                return new ContainerFiller(player, filler);
            }
        }
        if (id == BCBuildersGuis.ARCHITECT.ordinal()) {
            if (tile instanceof TileArchitectTable) {
                TileArchitectTable architectTable = (TileArchitectTable) tile;
                return new ContainerArchitectTable(player, architectTable);
            }
        }
        if (id == BCBuildersGuis.REPLACER.ordinal()) {
            if (tile instanceof TileReplacer) {
                TileReplacer replacer = (TileReplacer) tile;
                return new ContainerReplacer(player, replacer);
            }
        }
        if (id == BCBuildersGuis.FILLING_PLANNER.ordinal()) {
            return new ContainerFillingPlanner(player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }
    

    public void fmlPreInit() {
        
    }

    public void fmlInit() {

    }

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCBuildersProxy {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCBuildersProxy {
        @Override
        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (id == BCBuildersGuis.LIBRARY.ordinal()) {
                if (tile instanceof TileElectronicLibrary) {
                    TileElectronicLibrary library = (TileElectronicLibrary) tile;
                    return new GuiElectronicLibrary(new ContainerElectronicLibrary(player, library));
                }
            }
            if (id == BCBuildersGuis.BUILDER.ordinal()) {
                if (tile instanceof TileBuilder) {
                    TileBuilder builder = (TileBuilder) tile;
                    return new GuiBuilder(new ContainerBuilder(player, builder));
                }
            }
            if (id == BCBuildersGuis.FILLER.ordinal()) {
                if (tile instanceof TileFiller) {
                    TileFiller filler = (TileFiller) tile;
                    return new GuiFilling(new ContainerFiller(player, filler));
                }
            }
            if (id == BCBuildersGuis.ARCHITECT.ordinal()) {
                if (tile instanceof TileArchitectTable) {
                    TileArchitectTable architectTable = (TileArchitectTable) tile;
                    return new GuiArchitectTable(new ContainerArchitectTable(player, architectTable));
                }
            }
            if (id == BCBuildersGuis.REPLACER.ordinal()) {
                if (tile instanceof TileReplacer) {
                    TileReplacer replacer = (TileReplacer) tile;
                    return new GuiReplacer(new ContainerReplacer(player, replacer));
                }
            }
            if (id == BCBuildersGuis.FILLING_PLANNER.ordinal()) {
                return new GuiFilling(new ContainerFillingPlanner(player));
            }
            return null;
        }

        @Override
        public void fmlPreInit() {
            if (!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
                Minecraft.getMinecraft().getFramebuffer().enableStencil();
            }
            BCBuildersSprites.fmlPreInit();
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            ClientRegistry.bindTileEntitySpecialRenderer(TileArchitectTable.class, new RenderArchitectTable());
            ClientRegistry.bindTileEntitySpecialRenderer(TileBuilder.class, new RenderBuilder());
            ClientRegistry.bindTileEntitySpecialRenderer(TileFiller.class, new RenderFiller());
            ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, new RenderQuarry());
            DetachedRenderer.INSTANCE.addRenderer(
                DetachedRenderer.RenderMatrixType.FROM_WORLD_ORIGIN,
                RenderArchitectTables.INSTANCE
            );
        }
    }
}
