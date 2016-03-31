/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import buildcraft.BuildCraftTransport;
import buildcraft.core.lib.client.render.FluidRenderer;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.client.model.FacadeItemModel;
import buildcraft.transport.client.model.FacadePluggableModel;
import buildcraft.transport.client.model.GateItemModel;
import buildcraft.transport.client.model.GatePluggableModel;
import buildcraft.transport.client.model.LensPluggableModel;
import buildcraft.transport.client.model.ModelPowerAdapter;
import buildcraft.transport.client.model.PlugPluggableModel;
import buildcraft.transport.client.render.PipeRendererTESR;
import buildcraft.transport.client.render.PipeTransportRenderer;
import buildcraft.transport.client.render.PipeTransportRendererFluids;
import buildcraft.transport.client.render.PipeTransportRendererItems;
import buildcraft.transport.client.render.PipeTransportRendererPower;

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
    public void preInit() {
        super.preInit();

        Item transformer = Item.getItemFromBlock(BuildCraftTransport.ic2compattransformeroflols);
        ModelLoader.setCustomModelResourceLocation(transformer, 0, new ModelResourceLocation("buildcrafttransport:transformer", "facing=east,voltage=low_medium"));
        ModelLoader.setCustomModelResourceLocation(transformer, 1, new ModelResourceLocation("buildcrafttransport:transformer", "facing=east,voltage=medium_high"));
        ModelLoader.setCustomModelResourceLocation(transformer, 2, new ModelResourceLocation("buildcrafttransport:transformer", "facing=east,voltage=high_extreme"));
    }

    @Override
    public void registerRenderers() {
        /* Should none of these register? Should they all be called by a single modelBakeEvent, textureStitchPre and
         * textureStitchPost? */

        // MinecraftForge.EVENT_BUS.register(FluidShaderManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FacadePluggableModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FacadeItemModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(GatePluggableModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(GateItemModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(LensPluggableModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PlugPluggableModel.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ModelPowerAdapter.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FluidRenderer.INSTANCE);

        PipeTransportRenderer.RENDERER_MAP.put(PipeTransportItems.class, new PipeTransportRendererItems());
        PipeTransportRenderer.RENDERER_MAP.put(PipeTransportFluids.class, new PipeTransportRendererFluids());
        PipeTransportRenderer.RENDERER_MAP.put(PipeTransportPower.class, new PipeTransportRendererPower());
    }

    @Override
    public void setIconProviderFromPipe(ItemPipe item, Pipe<?> dummyPipe) {
        item.setPipesIcons(dummyPipe.getIconProvider());
    }
}
