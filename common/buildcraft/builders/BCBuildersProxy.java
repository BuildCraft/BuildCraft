/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

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

import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.MessageManager.MessageId;

import buildcraft.builders.client.render.RenderArchitectTable;
import buildcraft.builders.client.render.RenderArchitectTables;
import buildcraft.builders.client.render.RenderBuilder;
import buildcraft.builders.client.render.RenderFiller;
import buildcraft.builders.client.render.RenderQuarry;
import buildcraft.builders.container.ContainerArchitectTable;
import buildcraft.builders.container.ContainerBuilder;
import buildcraft.builders.container.ContainerElectronicLibrary;
import buildcraft.builders.container.ContainerFiller;
import buildcraft.builders.container.ContainerFillerPlanner;
import buildcraft.builders.container.ContainerReplacer;
import buildcraft.builders.gui.GuiArchitectTable;
import buildcraft.builders.gui.GuiBuilder;
import buildcraft.builders.gui.GuiElectronicLibrary;
import buildcraft.builders.gui.GuiFiller;
import buildcraft.builders.gui.GuiFillerPlanner;
import buildcraft.builders.gui.GuiReplacer;
import buildcraft.builders.snapshot.MessageSnapshotRequest;
import buildcraft.builders.snapshot.MessageSnapshotResponse;
import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileElectronicLibrary;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.builders.tile.TileReplacer;

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
            return new ContainerFillerPlanner(player);
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
        @Override
        public void fmlPreInit() {
            MessageManager.addType(MessageId.BC_BUILDERS_SNAPSHOT_REQUEST, MessageSnapshotRequest.class,
                MessageSnapshotRequest.HANDLER, Side.SERVER);
            MessageManager.addTypeSent(MessageId.BC_BUILDERS_SNAPSHOT_REPLY, MessageSnapshotResponse.class, Side.CLIENT);
        }
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
                    return new GuiFiller(new ContainerFiller(player, filler));
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
                return new GuiFillerPlanner(new ContainerFillerPlanner(player));
            }
            return null;
        }

        @Override
        public void fmlPreInit() {
            if (!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
                Minecraft.getMinecraft().getFramebuffer().enableStencil();
            }
            BCBuildersSprites.fmlPreInit();
            MessageManager.addTypeSent(MessageId.BC_BUILDERS_SNAPSHOT_REQUEST, MessageSnapshotRequest.class, Side.SERVER);
            MessageManager.addType(MessageId.BC_BUILDERS_SNAPSHOT_REPLY, MessageSnapshotResponse.class,
                MessageSnapshotResponse.HANDLER, Side.CLIENT);
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            ClientRegistry.bindTileEntitySpecialRenderer(TileArchitectTable.class, new RenderArchitectTable());
            ClientRegistry.bindTileEntitySpecialRenderer(TileBuilder.class, new RenderBuilder());
            ClientRegistry.bindTileEntitySpecialRenderer(TileFiller.class, new RenderFiller());
            ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, new RenderQuarry());
            DetachedRenderer.INSTANCE.addRenderer(DetachedRenderer.RenderMatrixType.FROM_WORLD_ORIGIN,
                RenderArchitectTables.INSTANCE);
        }
    }
}
