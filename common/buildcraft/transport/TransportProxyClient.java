/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.MinecraftForgeClient;

import buildcraft.BuildCraftTransport;
import buildcraft.transport.render.FacadeItemRenderer;
import buildcraft.transport.render.GateItemRenderer;
import buildcraft.transport.render.PipeItemRenderer;
import buildcraft.transport.render.PipeRendererTESR;
import buildcraft.transport.render.PipeRendererWorld;
import buildcraft.transport.render.PipeTransportFluidsRenderer;
import buildcraft.transport.render.PipeTransportItemsRenderer;
import buildcraft.transport.render.PipeTransportPowerRenderer;
import buildcraft.transport.render.PipeTransportRenderer;
import buildcraft.transport.render.PlugItemRenderer;
import buildcraft.transport.render.TileEntityPickupFX;

public class TransportProxyClient extends TransportProxy {
	public static final PipeItemRenderer pipeItemRenderer = new PipeItemRenderer();
	public static final PipeRendererWorld pipeWorldRenderer = new PipeRendererWorld();
	public static final FacadeItemRenderer facadeItemRenderer = new FacadeItemRenderer();
	public static final PlugItemRenderer plugItemRenderer = new PlugItemRenderer();
	public static final GateItemRenderer gateItemRenderer = new GateItemRenderer();

	@Override
	public void registerTileEntities() {
		super.registerTileEntities();
		ClientRegistry.bindTileEntitySpecialRenderer(TileGenericPipe.class, PipeRendererTESR.INSTANCE);
	}

	@Override
	public void obsidianPipePickup(World world, EntityItem item, TileEntity tile) {
		FMLClientHandler.instance().getClient().effectRenderer.addEffect(new TileEntityPickupFX(world, item, tile));
	}

	@Override
	public void clearDisplayList(int displayList) {
		GLAllocation.deleteDisplayLists(displayList);
	}

	@Override
	public void registerRenderers() {
		for (Item itemPipe : BlockGenericPipe.pipes.keySet()) {
			MinecraftForgeClient.registerItemRenderer(itemPipe, pipeItemRenderer);
		}

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.facadeItem, facadeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.plugItem, plugItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeGate, gateItemRenderer);

		PipeTransportRenderer.RENDERER_MAP.put(PipeTransportItems.class, new PipeTransportItemsRenderer());
		PipeTransportRenderer.RENDERER_MAP.put(PipeTransportFluids.class, new PipeTransportFluidsRenderer());
		PipeTransportRenderer.RENDERER_MAP.put(PipeTransportPower.class, new PipeTransportPowerRenderer());

		TransportProxy.pipeModel = RenderingRegistry.getNextAvailableRenderId();

		RenderingRegistry.registerBlockHandler(pipeWorldRenderer);
	}

	@Override
	public void setIconProviderFromPipe(ItemPipe item, Pipe<?> dummyPipe) {
		item.setPipesIcons(dummyPipe.getIconProvider());
	}
}
