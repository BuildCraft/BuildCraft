package buildcraft.transport;

import net.minecraftforge.client.MinecraftForgeClient;
import buildcraft.BuildCraftTransport;
import buildcraft.transport.render.FacadeItemRenderer;
import buildcraft.transport.render.PipeItemRenderer;
import buildcraft.transport.render.PipeWorldRenderer;
import buildcraft.transport.render.RenderPipe;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class TransportProxyClient extends TransportProxy {
	public final static PipeItemRenderer pipeItemRenderer = new PipeItemRenderer();
	public final static PipeWorldRenderer pipeWorldRenderer = new PipeWorldRenderer();
	public final static FacadeItemRenderer facadeItemRenderer = new FacadeItemRenderer();

	@Override
	public void registerTileEntities() {
		super.registerTileEntities();
		RenderPipe rp = new RenderPipe();
		ClientRegistry.bindTileEntitySpecialRenderer(TileDummyGenericPipe.class, rp);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDummyGenericPipe2.class, rp);
		ClientRegistry.bindTileEntitySpecialRenderer(TileGenericPipe.class, rp);
	}

	@Override
	public void registerRenderers() {

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsWood.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsCobblestone.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsStone.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsIron.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsGold.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsDiamond.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsObsidian.shiftedIndex, pipeItemRenderer);

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsWood.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsCobblestone.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsStone.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsIron.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsGold.shiftedIndex, pipeItemRenderer);

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerWood.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerStone.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerGold.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeStructureCobblestone.shiftedIndex, pipeItemRenderer);
		// MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsStipes.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsVoid.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsVoid.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsSandstone.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsSandstone.shiftedIndex, pipeItemRenderer);

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.facadeItem.shiftedIndex, facadeItemRenderer);
		TransportProxy.pipeModel = RenderingRegistry.getNextAvailableRenderId();

		RenderingRegistry.registerBlockHandler(pipeWorldRenderer);
	}
}
