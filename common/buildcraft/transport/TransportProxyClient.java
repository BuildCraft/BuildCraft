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

import buildcraft.core.lib.render.FluidRenderer;
import buildcraft.transport.pluggable.LensPluggableModel;
import buildcraft.transport.pluggable.PlugPluggableModel;
import buildcraft.transport.pluggable.PowerAdapterModel;
import buildcraft.transport.render.FacadeItemModel;
import buildcraft.transport.render.FacadePluggableModel;
import buildcraft.transport.render.GateItemModel;
import buildcraft.transport.render.GatePluggableModel;
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
        // Should none of these register? Should they all be called by  a single modelBakeEvent, textureStitchPre and textureStitchPost?
        
        // MinecraftForge.EVENT_BUS.register(FluidShaderManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FacadePluggableModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FacadeItemModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(GatePluggableModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(GateItemModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(LensPluggableModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PlugPluggableModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PowerAdapterModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FluidRenderer.INSTANCE);
    }

    @Override
    public void setIconProviderFromPipe(ItemPipe item, Pipe<?> dummyPipe) {
        item.setPipesIcons(dummyPipe.getIconProvider());
    }
}
