/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.container.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.builders.client.render.RenderArchitect;
import buildcraft.builders.client.render.RenderBuilder;
import buildcraft.builders.client.render.RenderFiller;
import buildcraft.builders.client.render.RenderQuarry;
import buildcraft.builders.entity.EntityQuarry;
import buildcraft.builders.gui.*;
import buildcraft.builders.tile.TileArchitect;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileLibrary;
import buildcraft.builders.tile.TileQuarry;

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
            if (tile instanceof TileLibrary) {
                TileLibrary library = (TileLibrary) tile;
                return new ContainerBlueprintLibrary(player, library);
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
            if (tile instanceof TileArchitect) {
                TileArchitect library = (TileArchitect) tile;
                return new ContainerArchitect(player, library);
            }
        }
        if (id == BCBuildersGuis.QUARRY.ordinal()) {
            if (tile instanceof TileQuarry) {
                TileQuarry quarry = (TileQuarry) tile;
                return new ContainerQuarry(player, quarry);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlInit() {
        EntityRegistry.registerModEntity(EntityQuarry.class, "quarry", 0, BCBuilders.INSTANCE, 50, 1, true);
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
                if (tile instanceof TileLibrary) {
                    TileLibrary library = (TileLibrary) tile;
                    return new GuiBlueprintLibrary(player, library);
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
                if (tile instanceof TileArchitect) {
                    TileArchitect library = (TileArchitect) tile;
                    return new GuiArchitect(new ContainerArchitect(player, library));
                }
            }
            if (id == BCBuildersGuis.QUARRY.ordinal()) {
                if (tile instanceof TileQuarry) {
                    TileQuarry quarry = (TileQuarry) tile;
                    return new GuiQuarry(new ContainerQuarry(player, quarry));
                }
            }
            return null;
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            ClientRegistry.bindTileEntitySpecialRenderer(TileArchitect.class, new RenderArchitect());
            ClientRegistry.bindTileEntitySpecialRenderer(TileBuilder.class, new RenderBuilder());
            ClientRegistry.bindTileEntitySpecialRenderer(TileFiller.class, new RenderFiller());
            ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, new RenderQuarry());
        }
    }
}
