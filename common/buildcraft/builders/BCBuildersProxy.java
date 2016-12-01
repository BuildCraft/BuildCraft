/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

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
import buildcraft.builders.container.ContainerArchitect;
import buildcraft.builders.container.ContainerBlueprintLibrary;
import buildcraft.builders.container.ContainerBuilder_Neptune;
import buildcraft.builders.container.ContainerQuarry;
import buildcraft.builders.entity.EntityQuarry;
import buildcraft.builders.gui.GuiArchitect;
import buildcraft.builders.gui.GuiBlueprintLibrary;
import buildcraft.builders.gui.GuiBuilder_Neptune;
import buildcraft.builders.gui.GuiQuarry;
import buildcraft.builders.tile.TileArchitect_Neptune;
import buildcraft.builders.tile.TileBuilder_Neptune;
import buildcraft.builders.tile.TileFiller_Neptune;
import buildcraft.builders.tile.TileLibrary_Neptune;
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
            if (tile instanceof TileLibrary_Neptune) {
                TileLibrary_Neptune library = (TileLibrary_Neptune) tile;
                return new ContainerBlueprintLibrary(player, library);
            }
        }
        if (id == BCBuildersGuis.BUILDER.ordinal()) {
            if (tile instanceof TileBuilder_Neptune) {
                TileBuilder_Neptune builder = (TileBuilder_Neptune) tile;
                return new ContainerBuilder_Neptune(player, builder);
            }
        }
        if (id == BCBuildersGuis.ARCHITECT.ordinal()) {
            if (tile instanceof TileArchitect_Neptune) {
                TileArchitect_Neptune library = (TileArchitect_Neptune) tile;
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
                if (tile instanceof TileLibrary_Neptune) {
                    TileLibrary_Neptune library = (TileLibrary_Neptune) tile;
                    return new GuiBlueprintLibrary(player, library);
                }
            }
            if (id == BCBuildersGuis.BUILDER.ordinal()) {
                if (tile instanceof TileBuilder_Neptune) {
                    TileBuilder_Neptune builder = (TileBuilder_Neptune) tile;
                    return new GuiBuilder_Neptune(new ContainerBuilder_Neptune(player, builder));
                }
            }
            if (id == BCBuildersGuis.ARCHITECT.ordinal()) {
                if (tile instanceof TileArchitect_Neptune) {
                    TileArchitect_Neptune library = (TileArchitect_Neptune) tile;
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
            ClientRegistry.bindTileEntitySpecialRenderer(TileArchitect_Neptune.class, new RenderArchitect());
            ClientRegistry.bindTileEntitySpecialRenderer(TileBuilder_Neptune.class, new RenderBuilder());
            ClientRegistry.bindTileEntitySpecialRenderer(TileFiller_Neptune.class, new RenderFiller());
            ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, new RenderQuarry());
        }
    }
}
