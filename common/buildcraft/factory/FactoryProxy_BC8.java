/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import buildcraft.factory.client.render.RenderMiningWell;
import buildcraft.factory.client.render.RenderPump;
import buildcraft.factory.client.render.RenderTube;
import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.lib.client.render.tile.RenderMultiRenderers;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

        @Override
        public void fmlInit() {
            TileMiningWell.TUBE_END_TEXTURE = SpriteHolderRegistry.getHolder(new ResourceLocation("buildcraftfactory", "blocks/plain_pipe/end"));
            TileMiningWell.TUBE_SIDE_TEXTURE = SpriteHolderRegistry.getHolder(new ResourceLocation("buildcraftfactory", "blocks/plain_pipe/side"));
            ClientRegistry.bindTileEntitySpecialRenderer(TileMiningWell.class, new RenderMultiRenderers(new RenderMiningWell(), new RenderTube()));
            TilePump.TUBE_END_TEXTURE = SpriteHolderRegistry.getHolder(new ResourceLocation("buildcraftfactory", "blocks/tube/end"));
            TilePump.TUBE_SIDE_TEXTURE = SpriteHolderRegistry.getHolder(new ResourceLocation("buildcraftfactory", "blocks/tube/side"));
            ClientRegistry.bindTileEntitySpecialRenderer(TilePump.class, new RenderMultiRenderers(new RenderPump(), new RenderTube()));
        }
    }
}
