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
	public void loadTerrainIcons(BuildCraftTransport instance) {
		instance.terrainIcons = new Icon[IconTerrainConstants.MAX];
		TextureMap terrainTextures = Minecraft.getMinecraft().renderEngine.field_94154_l;
		
		instance.terrainIcons[IconTerrainConstants.PipeStructureCobblestone] = terrainTextures.func_94245_a("buildcraft:pipeStructureCobblestone");
		
		instance.terrainIcons[IconTerrainConstants.PipeItemsCobbleStone] = terrainTextures.func_94245_a("buildcraft:pipeItemsCobblestone");
		
		instance.terrainIcons[IconTerrainConstants.PipeItemsDiamond_Center] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_center");
		instance.terrainIcons[IconTerrainConstants.PipeItemsDiamond_Down] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_down");
		instance.terrainIcons[IconTerrainConstants.PipeItemsDiamond_Up] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_up");
		instance.terrainIcons[IconTerrainConstants.PipeItemsDiamond_North] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_north");
		instance.terrainIcons[IconTerrainConstants.PipeItemsDiamond_South] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_south");
		instance.terrainIcons[IconTerrainConstants.PipeItemsDiamond_West] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_west");
		instance.terrainIcons[IconTerrainConstants.PipeItemsDiamond_East] = terrainTextures.func_94245_a("buildcraft:pipeItemsDiamond_east");
		
		instance.terrainIcons[IconTerrainConstants.PipeItemsWood_Standard] = terrainTextures.func_94245_a("buildcraft:pipeItemsWood_standard");
		instance.terrainIcons[IconTerrainConstants.PipeAllWood_Solid] = terrainTextures.func_94245_a("buildcraft:pipeAllWood_solid");
		
		instance.terrainIcons[IconTerrainConstants.PipeItemsEmerald_Standard] = terrainTextures.func_94245_a("buildcraft:pipeItemsEmerald_standard");
		instance.terrainIcons[IconTerrainConstants.PipeAllEmerald_Solid] = terrainTextures.func_94245_a("buildcraft:pipeAllEmerald_solid");
		
		instance.terrainIcons[IconTerrainConstants.PipeItemsGold] = terrainTextures.func_94245_a("buildcraft:pipeItemsGold");
		
		instance.terrainIcons[IconTerrainConstants.PipeItemsIron_Standard] = terrainTextures.func_94245_a("buildcraft:pipeItemsIron_standard");
		instance.terrainIcons[IconTerrainConstants.PipeAllIron_Solid] = terrainTextures.func_94245_a("buildcraft:pipeAllIron_solid");
		
		instance.terrainIcons[IconTerrainConstants.PipeItemsObsidian] = terrainTextures.func_94245_a("buildcraft:pipeItemsObsidian");
		instance.terrainIcons[IconTerrainConstants.PipeItemsSandstone] = terrainTextures.func_94245_a("buildcraft:pipeItemsSandstone");
		instance.terrainIcons[IconTerrainConstants.PipeItemsStone] = terrainTextures.func_94245_a("buildcraft:pipeItemsStone");
		instance.terrainIcons[IconTerrainConstants.PipeItemsVoid] = terrainTextures.func_94245_a("buildcraft:pipeItemsVoid");
		
		instance.terrainIcons[IconTerrainConstants.PipeLiquidsCobblestone] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsCobblestone");
		instance.terrainIcons[IconTerrainConstants.PipeLiquidsWood_Standard] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsWood_standard");
		instance.terrainIcons[IconTerrainConstants.PipeLiquidsEmerald_Standard] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsEmerald_standard");
		instance.terrainIcons[IconTerrainConstants.PipeLiquidsGold] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsGold");
		instance.terrainIcons[IconTerrainConstants.PipeLiquidsIron_Standard] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsIron_standard");
		instance.terrainIcons[IconTerrainConstants.PipeLiquidsSandstone] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsSandstone");
		instance.terrainIcons[IconTerrainConstants.PipeLiquidsStone] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsStone");
		instance.terrainIcons[IconTerrainConstants.PipeLiquidsVoid] = terrainTextures.func_94245_a("buildcraft:pipeLiquidsVoid");
		
		instance.terrainIcons[IconTerrainConstants.PipePowerGold] = terrainTextures.func_94245_a("buildcraft:pipePowerGold");
		instance.terrainIcons[IconTerrainConstants.PipePowerStone] = terrainTextures.func_94245_a("buildcraft:pipePowerStone");
		instance.terrainIcons[IconTerrainConstants.PipePowerWood_Standard] = terrainTextures.func_94245_a("buildcraft:pipePowerWood_standard");
		
		instance.terrainIcons[IconTerrainConstants.Texture_Red_Dark] = terrainTextures.func_94245_a("buildcraft:texture_red_dark");
		instance.terrainIcons[IconTerrainConstants.Texture_Red_Lit] = terrainTextures.func_94245_a("buildcraft:texture_red_lit");
		instance.terrainIcons[IconTerrainConstants.Texture_Blue_Dark] = terrainTextures.func_94245_a("buildcraft:texture_blue_dark");
		instance.terrainIcons[IconTerrainConstants.Texture_Blue_Lit] = terrainTextures.func_94245_a("buildcraft:texture_blue_lit");
		instance.terrainIcons[IconTerrainConstants.Texture_Green_Dark] = terrainTextures.func_94245_a("buildcraft:texture_green_dark");
		instance.terrainIcons[IconTerrainConstants.Texture_Green_Lit] = terrainTextures.func_94245_a("buildcraft:texture_green_lit");
		instance.terrainIcons[IconTerrainConstants.Texture_Yellow_Dark] = terrainTextures.func_94245_a("buildcraft:texture_yellow_dark");
		instance.terrainIcons[IconTerrainConstants.Texture_Yellow_Lit] = terrainTextures.func_94245_a("buildcraft:texture_yellow_lit");
		instance.terrainIcons[IconTerrainConstants.Texture_Cyan] = terrainTextures.func_94245_a("buildcraft:texture_cyan");
		
		instance.terrainIcons[IconTerrainConstants.Gate_Dark] = terrainTextures.func_94245_a("buildcraft:gate_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Lit] = terrainTextures.func_94245_a("buildcraft:gate_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Iron_And_Dark] = terrainTextures.func_94245_a("buildcraft:gate_iron_and_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Iron_And_Lit] = terrainTextures.func_94245_a("buildcraft:gate_iron_and_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Iron_Or_Dark] = terrainTextures.func_94245_a("buildcraft:gate_iron_or_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Iron_Or_Lit] = terrainTextures.func_94245_a("buildcraft:gate_iron_or_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Gold_And_Dark] = terrainTextures.func_94245_a("buildcraft:gate_gold_and_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Gold_And_Lit] = terrainTextures.func_94245_a("buildcraft:gate_gold_and_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Gold_Or_Dark] = terrainTextures.func_94245_a("buildcraft:gate_gold_or_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Gold_Or_Lit] = terrainTextures.func_94245_a("buildcraft:gate_gold_or_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Diamond_And_Dark] = terrainTextures.func_94245_a("buildcraft:gate_diamond_and_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Diamond_And_Lit] = terrainTextures.func_94245_a("buildcraft:gate_diamond_and_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Diamond_Or_Dark] = terrainTextures.func_94245_a("buildcraft:gate_diamond_or_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Diamond_Or_Lit] = terrainTextures.func_94245_a("buildcraft:gate_diamond_or_lit");
		
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Dark] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Lit] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Iron_And_Dark] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_iron_and_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Iron_And_Lit] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_iron_and_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Iron_Or_Dark] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_iron_or_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Iron_Or_Lit] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_iron_or_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Gold_And_Dark] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_gold_and_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Gold_And_Lit] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_gold_and_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Gold_Or_Dark] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_gold_or_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Gold_Or_Lit] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_gold_or_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Diamond_And_Dark] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_diamond_and_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Diamond_And_Lit] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_diamond_and_lit");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Diamond_Or_Dark] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_diamond_or_dark");
		instance.terrainIcons[IconTerrainConstants.Gate_Autarchic_Diamond_Or_Lit] = terrainTextures.func_94245_a("buildcraft:gate_autarchic_diamond_or_lit");	
	}
	
	@Override
	public void loadItemIcons(BuildCraftTransport instance) {
		instance.itemIcons = new Icon[IconItemConstants.MAX];
		TextureMap itemTextures = Minecraft.getMinecraft().renderEngine.field_94155_m;
		
		instance.itemIcons[IconItemConstants.Gate] = itemTextures.func_94245_a("buildcraft:gate");
		instance.itemIcons[IconItemConstants.Gate_Iron_And] = itemTextures.func_94245_a("buildcraft:gate_iron_and");
		instance.itemIcons[IconItemConstants.Gate_Iron_Or] = itemTextures.func_94245_a("buildcraft:gate_iron_or");
		instance.itemIcons[IconItemConstants.Gate_Gold_And] = itemTextures.func_94245_a("buildcraft:gate_gold_and");
		instance.itemIcons[IconItemConstants.Gate_Gold_Or] = itemTextures.func_94245_a("buildcraft:gate_gold_or");
		instance.itemIcons[IconItemConstants.Gate_Diamond_And] = itemTextures.func_94245_a("buildcraft:gate_diamond_and");
		instance.itemIcons[IconItemConstants.Gate_Diamond_Or] = itemTextures.func_94245_a("buildcraft:gate_diamond_or");
		
		instance.itemIcons[IconItemConstants.Autarchic_Gate] = itemTextures.func_94245_a("buildcraft:autarchic_gate");
		instance.itemIcons[IconItemConstants.Autarchic_Gate_Iron_And] = itemTextures.func_94245_a("buildcraft:autarchic_gate_iron_and");
		instance.itemIcons[IconItemConstants.Autarchic_Gate_Iron_Or] = itemTextures.func_94245_a("buildcraft:autarchic_gate_iron_or");
		instance.itemIcons[IconItemConstants.Autarchic_Gate_Gold_And] = itemTextures.func_94245_a("buildcraft:autarchic_gate_gold_and");
		instance.itemIcons[IconItemConstants.Autarchic_Gate_Gold_Or] = itemTextures.func_94245_a("buildcraft:autarchic_gate_gold_or");
		instance.itemIcons[IconItemConstants.Autarchic_Gate_Diamond_And] = itemTextures.func_94245_a("buildcraft:autarchic_gate_diamond_and");
		instance.itemIcons[IconItemConstants.Autarchic_Gate_Diamond_Or] = itemTextures.func_94245_a("buildcraft:autarchic_gate_diamond_or");

	}
}
