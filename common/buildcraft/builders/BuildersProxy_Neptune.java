/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.client.render.RenderArchitect;
import buildcraft.builders.container.ContainerArchitect;
import buildcraft.builders.gui.GuiArchitect;
import buildcraft.builders.tile.TileArchitect_Neptune;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.builders.gui.ContainerBlueprintLibrary;
import buildcraft.builders.gui.GuiBlueprintLibrary;
import buildcraft.builders.tile.TileLibrary_Neptune;

public abstract class BuildersProxy_Neptune implements IGuiHandler {
    @SidedProxy
    private static BuildersProxy_Neptune proxy;

    public static BuildersProxy_Neptune getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == BuildersGuis.LIBRARY.ordinal()) {
            if (tile instanceof TileLibrary_Neptune) {
                TileLibrary_Neptune library = (TileLibrary_Neptune) tile;
                return new ContainerBlueprintLibrary(player, library);
            }
        }
        if (ID == BuildersGuis.ARCHITECT.ordinal()) {
            if (tile instanceof TileArchitect_Neptune) {
                TileArchitect_Neptune library = (TileArchitect_Neptune) tile;
                return new ContainerArchitect(player, library);
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
    public static class ServerProxy extends BuildersProxy_Neptune {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BuildersProxy_Neptune {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (ID == BuildersGuis.LIBRARY.ordinal()) {
                if (tile instanceof TileLibrary_Neptune) {
                    TileLibrary_Neptune library = (TileLibrary_Neptune) tile;
                    return new GuiBlueprintLibrary(player, library);
                }
            }
            if (ID == BuildersGuis.ARCHITECT.ordinal()) {
                if (tile instanceof TileArchitect_Neptune) {
                    TileArchitect_Neptune library = (TileArchitect_Neptune) tile;
                    return new GuiArchitect(new ContainerArchitect(player, library));
                }
            }
            return null;
        }
        
        @Override
        public void fmlInit() {
            super.fmlInit();
            ClientRegistry.bindTileEntitySpecialRenderer(TileArchitect_Neptune.class, new RenderArchitect());
        }
    }
}
