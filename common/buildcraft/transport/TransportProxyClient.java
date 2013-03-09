package buildcraft.transport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Icon;
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

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsWood.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsCobblestone.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsStone.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsIron.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsGold.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsDiamond.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsObsidian.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsEmerald.itemID, pipeItemRenderer);

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsWood.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsCobblestone.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsStone.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsIron.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsGold.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsEmerald.itemID, pipeItemRenderer);

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerWood.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerStone.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipePowerGold.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeStructureCobblestone.itemID, pipeItemRenderer);
		// MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsStipes.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsVoid.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsVoid.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsSandstone.itemID, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsSandstone.itemID, pipeItemRenderer);

		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.facadeItem.itemID, facadeItemRenderer);
		TransportProxy.pipeModel = RenderingRegistry.getNextAvailableRenderId();

		RenderingRegistry.registerBlockHandler(pipeWorldRenderer);
	}
	
	@Override
	public void loadIcons(BuildCraftTransport instance) {
		instance.icons = new Icon[IconConstants.MAX];
		TextureMap terrainTextures = Minecraft.getMinecraft().renderEngine.field_94154_l;
		instance.icons[IconConstants.PipeStructureCobblestone] = terrainTextures.func_94245_a("buildcraft:pipeStructureCobblestone");
		
		instance.icons[IconConstants.PipeItemsCobbleStone] = terrainTextures.func_94245_a("buildcraft:pipeItemsCobblestone");
		
		instance.icons[IconConstants.PipeItemsDiamond_Center] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_center");
		instance.icons[IconConstants.PipeItemsDiamond_Down] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_down");
		instance.icons[IconConstants.PipeItemsDiamond_Up] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_up");
		instance.icons[IconConstants.PipeItemsDiamond_North] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_north");
		instance.icons[IconConstants.PipeItemsDiamond_South] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_south");
		instance.icons[IconConstants.PipeItemsDiamond_West] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_west");
		instance.icons[IconConstants.PipeItemsDiamond_East] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_east");
		
		instance.icons[IconConstants.PipeItemsWood_Standard] = terrainTextures.func_94245_a("buildcraft:pipeItemsWood_standard");
		instance.icons[IconConstants.PipeAllWood_Solid] = terrainTextures.func_94245_a("buildcraft:pipeAllWood_solid");
		
		instance.icons[IconConstants.PipeItemsEmerald_Standard] = terrainTextures.func_94245_a("buildcraft:pipeItemsEmerald_standard");
		instance.icons[IconConstants.PipeAllEmerald_Solid] = terrainTextures.func_94245_a("buildcraft:pipeAllEmerald_solid");
		
		instance.icons[IconConstants.PipeItemsGold] = terrainTextures.func_94245_a("buildcraft:pipeItemsGold");
		
		instance.icons[IconConstants.PipeItemsIron_Standard] = terrainTextures.func_94245_a("buildcraft:pipeItemsIron_standard");
		instance.icons[IconConstants.PipeAllIron_Solid] = terrainTextures.func_94245_a("buildcraft:pipeAllIron_solid");
		
		instance.icons[IconConstants.PipeItemsObsidian] = terrainTextures.func_94245_a("buildcraft:pipeItemsObsidian");
		instance.icons[IconConstants.PipeItemsSandstone] = terrainTextures.func_94245_a("buildcraft:pipeItemsSandstone");
		instance.icons[IconConstants.PipeItemsStone] = terrainTextures.func_94245_a("buildcraft:pipeItemsStone");
		instance.icons[IconConstants.PipeItemsVoid] = terrainTextures.func_94245_a("buildcraft:pipeItemsVoid");
		
		instance.icons[IconConstants.PipeLiquidsCobblestone] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsCobblestone");
		instance.icons[IconConstants.PipeLiquidsWood_Standard] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsWood_standard");
		instance.icons[IconConstants.PipeLiquidsEmerald_Standard] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsEmerald_standard");
		instance.icons[IconConstants.PipeLiquidsGold] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsGold");
		instance.icons[IconConstants.PipeLiquidsIron_Standard] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsIron_standard");
		instance.icons[IconConstants.PipeLiquidsSandstone] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsSandstone");
		instance.icons[IconConstants.PipeLiquidsStone] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsStone");
		instance.icons[IconConstants.PipeLiquidsVoid] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsVoid");
		
		instance.icons[IconConstants.PipePowerGold] = terrainTextures.func_94245_a("buildcraft:pipePowerGold");
		instance.icons[IconConstants.PipePowerStone] = terrainTextures.func_94245_a("buildcraft:pipePowerStone");
		instance.icons[IconConstants.PipePowerWood_Standard] = terrainTextures.func_94245_a("buildcraft:pipePowerWood_standard");
		
		instance.icons[IconConstants.Texture_Red_Dark] = terrainTextures.func_94245_a("buildcraft:texture_red_dark");
		instance.icons[IconConstants.Texture_Red_Lit] = terrainTextures.func_94245_a("buildcraft:texture_red_lit");
		instance.icons[IconConstants.Texture_Blue_Dark] = terrainTextures.func_94245_a("buildcraft:texture_blue_dark");
		instance.icons[IconConstants.Texture_Blue_Lit] = terrainTextures.func_94245_a("buildcraft:texture_blue_lit");
		instance.icons[IconConstants.Texture_Green_Dark] = terrainTextures.func_94245_a("buildcraft:texture_green_dark");
		instance.icons[IconConstants.Texture_Green_Lit] = terrainTextures.func_94245_a("buildcraft:texture_green_lit");
		instance.icons[IconConstants.Texture_Yellow_Dark] = terrainTextures.func_94245_a("buildcraft:texture_yellow_dark");
		instance.icons[IconConstants.Texture_Yellow_Lit] = terrainTextures.func_94245_a("buildcraft:texture_yellow_lit");
		instance.icons[IconConstants.Texture_Cyan] = terrainTextures.func_94245_a("buildcraft:texture_cyan");
		
		
	}
}
