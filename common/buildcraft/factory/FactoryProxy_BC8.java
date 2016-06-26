/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package buildcraft.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.tile.TileAutoWorkbenchItems;

public abstract class FactoryProxy_BC8 implements IGuiHandler {
    @SidedProxy
    private static FactoryProxy_BC8 proxy;

    public static FactoryProxy_BC8 getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == FactoryGuis.AUTO_WORKBENCH_ITEMS.ordinal()) {
            if (tile instanceof TileAutoWorkbenchItems) {
                TileAutoWorkbenchItems workbench = (TileAutoWorkbenchItems) tile;
                return new ContainerAutoCraftItems(player, workbench);
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
    public static class ServerProxy extends FactoryProxy_BC8 {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends FactoryProxy_BC8 {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (ID == FactoryGuis.AUTO_WORKBENCH_ITEMS.ordinal()) {
                if (tile instanceof TileAutoWorkbenchItems) {
                    TileAutoWorkbenchItems workbench = (TileAutoWorkbenchItems) tile;
                    return new GuiAutoCraftItems(new ContainerAutoCraftItems(player, workbench));
                }
            }
            return null;
        }
    }
}
