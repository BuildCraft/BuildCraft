/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

import net.minecraftforge.client.MinecraftForgeClient;

import buildcraft.BuildCraftTransport;
import buildcraft.transport.render.FacadeItemRenderer;
import buildcraft.transport.render.GateItemRenderer;
import buildcraft.transport.render.PipeItemRenderer;
import buildcraft.transport.render.PipeRendererTESR;
import buildcraft.transport.render.PipeRendererWorld;
import buildcraft.transport.render.PlugItemRenderer;
import buildcraft.transport.render.RobotStationItemRenderer;

public class TransportProxyClient extends TransportProxy {

	public static final PipeItemRenderer pipeItemRenderer = new PipeItemRenderer();
	public static final PipeRendererWorld pipeWorldRenderer = new PipeRendererWorld();
	public static final FacadeItemRenderer facadeItemRenderer = new FacadeItemRenderer();
	public static final PlugItemRenderer plugItemRenderer = new PlugItemRenderer();
	public static final RobotStationItemRenderer robotStationItemRenderer = new RobotStationItemRenderer();
	public static final GateItemRenderer gateItemRenderer = new GateItemRenderer();

	@Override
	public void registerTileEntities() {
		super.registerTileEntities();
		PipeRendererTESR rp = new PipeRendererTESR();
		ClientRegistry.bindTileEntitySpecialRenderer(TileDummyGenericPipe.class, rp);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDummyGenericPipe2.class, rp);
		ClientRegistry.bindTileEntitySpecialRenderer(TileGenericPipe.class, rp);
	}

	@Override
	public void registerRenderers() {

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsWood, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsCobblestone, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsStone, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsQuartz, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsIron, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsGold, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsDiamond, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsObsidian, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsEmerald, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsLapis, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsDaizuli, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsEmzuli, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsStripes, pipeItemRenderer);

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeFluidsWood, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeFluidsCobblestone, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeFluidsStone, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeFluidsIron, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeFluidsGold, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeFluidsEmerald, pipeItemRenderer);

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerWood, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerCobblestone, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerStone, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerQuartz, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerIron, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerGold, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerDiamond, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerHeat, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeStructureCobblestone, pipeItemRenderer);

		// MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsStipes.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsVoid, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeFluidsVoid, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsSandstone, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeFluidsSandstone, pipeItemRenderer);

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.facadeItem, facadeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.plugItem, plugItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.robotStationItem, robotStationItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeGate, gateItemRenderer);

		TransportProxy.pipeModel = RenderingRegistry.getNextAvailableRenderId();

		RenderingRegistry.registerBlockHandler(pipeWorldRenderer);
	}

	@Override
	public void setIconProviderFromPipe(ItemPipe item, Pipe dummyPipe) {
		item.setPipesIcons(dummyPipe.getIconProvider());
	}
}
