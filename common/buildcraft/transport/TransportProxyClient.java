/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import buildcraft.transport.pluggable.LensPluggableModel;
import buildcraft.transport.render.tile.PipeRendererTESR;

public class TransportProxyClient extends TransportProxy {

    @Override
    public void registerTileEntities() {
        super.registerTileEntities();
        PipeRendererTESR rp = new PipeRendererTESR();
        ClientRegistry.bindTileEntitySpecialRenderer(TileGenericPipe.class, rp);

        // ClientRegistry.bindTileEntitySpecialRenderer(TilePipe_BC8.class, new );
    }

    @Override
    public void obsidianPipePickup(World world, EntityItem item, TileEntity tile) {
        // FMLClientHandler.instance().getClient().effectRenderer.addEffect(new TileEntityPickupFX(world, item, tile));
    }

    @Override
    public void registerRenderers() {
        // MinecraftForge.EVENT_BUS.register(FluidShaderManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(LensPluggableModel.INSTANCE);
    }

    @Override
    public void setIconProviderFromPipe(ItemPipe item, Pipe<?> dummyPipe) {
        item.setPipesIcons(dummyPipe.getIconProvider());
    }
}
