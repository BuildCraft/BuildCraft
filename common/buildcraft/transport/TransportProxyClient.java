package buildcraft.transport;

import buildcraft.BuildCraftTransport;
import buildcraft.transport.render.FacadeItemRenderer;
import buildcraft.transport.render.GateItemRenderer;
import buildcraft.transport.render.PipeItemRenderer;
import buildcraft.transport.render.PipeRendererWorld;
import buildcraft.transport.render.PlugItemRenderer;
import buildcraft.transport.render.PipeRendererTESR;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.MinecraftForgeClient;

public class TransportProxyClient extends TransportProxy {

	public final static PipeItemRenderer pipeItemRenderer = new PipeItemRenderer();
	public final static PipeRendererWorld pipeWorldRenderer = new PipeRendererWorld();
	public final static FacadeItemRenderer facadeItemRenderer = new FacadeItemRenderer();
	public final static PlugItemRenderer plugItemRenderer = new PlugItemRenderer();
	public final static GateItemRenderer gateItemRenderer = new GateItemRenderer();

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
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeStructureCobblestone, pipeItemRenderer);
		// MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsStipes.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsVoid, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeFluidsVoid, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsSandstone, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeFluidsSandstone, pipeItemRenderer);

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.facadeItem, facadeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.plugItem, plugItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeGate, gateItemRenderer);
		TransportProxy.pipeModel = RenderingRegistry.getNextAvailableRenderId();

		RenderingRegistry.registerBlockHandler(pipeWorldRenderer);
	}

	@Override
	public void setIconProviderFromPipe(ItemPipe item, Pipe dummyPipe) {
		item.setPipesIcons(dummyPipe.getIconProvider());
	}
}
