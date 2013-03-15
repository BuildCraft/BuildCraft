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
	public void setIconProviderFromPipe(ItemPipe item, Pipe dummyPipe) {
		item.setPipesIcons(dummyPipe.getIconProvider());
	}

//	
//	@Override
//	public void loadItemIcons(BuildCraftTransport instance) {
//		TextureMap itemTextures = Minecraft.getMinecraft().renderEngine.field_94155_m;
//		
//		
//		instance.itemIcons[IconItemConstants.Action_MachineControl_On] = itemTextures.func_94245_a("buildcraft:triggers/action_machinecontrol_on");
//		instance.itemIcons[IconItemConstants.Action_MachineControl_Off] = itemTextures.func_94245_a("buildcraft:triggers/action_machinecontrol_off");
//		instance.itemIcons[IconItemConstants.Action_MachineControl_Loop] = itemTextures.func_94245_a("buildcraft:triggers/action_machinecontrol_loop");
//		
//		instance.itemIcons[IconItemConstants.Trigger_EngineHeat_Blue] = itemTextures.func_94245_a("buildcraft:triggers/trigger_engineheat_blue");
//		instance.itemIcons[IconItemConstants.Trigger_EngineHeat_Green] = itemTextures.func_94245_a("buildcraft:triggers/trigger_engineheat_green");
//		instance.itemIcons[IconItemConstants.Trigger_EngineHeat_Yellow] = itemTextures.func_94245_a("buildcraft:triggers/trigger_engineheat_yellow");
//		instance.itemIcons[IconItemConstants.Trigger_EngineHeat_Red] = itemTextures.func_94245_a("buildcraft:triggers/trigger_engineheat_red");
//		instance.itemIcons[IconItemConstants.Trigger_Inventory_Empty] = itemTextures.func_94245_a("buildcraft:triggers/trigger_inventory_empty");
//		instance.itemIcons[IconItemConstants.Trigger_Inventory_Contains] = itemTextures.func_94245_a("buildcraft:triggers/trigger_inventory_contains");
//		instance.itemIcons[IconItemConstants.Trigger_Inventory_Space] = itemTextures.func_94245_a("buildcraft:triggers/trigger_inventory_space");
//		instance.itemIcons[IconItemConstants.Trigger_Inventory_Full] = itemTextures.func_94245_a("buildcraft:triggers/trigger_inventory_full");
//		instance.itemIcons[IconItemConstants.Trigger_LiquidContainer_Empty] = itemTextures.func_94245_a("buildcraft:triggers/trigger_liquidcontainer_empty");
//		instance.itemIcons[IconItemConstants.Trigger_LiquidContainer_Contains] = itemTextures.func_94245_a("buildcraft:triggers/trigger_liquidcontainer_contains");
//		instance.itemIcons[IconItemConstants.Trigger_LiquidContainer_Space] = itemTextures.func_94245_a("buildcraft:triggers/trigger_liquidcontainer_space");
//		instance.itemIcons[IconItemConstants.Trigger_LiquidContainer_Full] = itemTextures.func_94245_a("buildcraft:triggers/trigger_liquidcontainer_full");
//		instance.itemIcons[IconItemConstants.Trigger_Machine_Active] = itemTextures.func_94245_a("buildcraft:triggers/trigger_machine_active");
//		instance.itemIcons[IconItemConstants.Trigger_Machine_Inactive] = itemTextures.func_94245_a("buildcraft:triggers/trigger_machine_inactive");
//		instance.itemIcons[IconItemConstants.Trigger_PipeContents_Empty] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipecontents_empty");
//		instance.itemIcons[IconItemConstants.Trigger_PipeContents_ContainsItems] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipecontents_containsitems");
//		instance.itemIcons[IconItemConstants.Trigger_PipeContents_ContainsLiquid] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipecontents_containsliquid");
//		instance.itemIcons[IconItemConstants.Trigger_PipeContents_ContainsEnergy] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipecontents_containsenergy");
//		instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Red_Active] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipesignal_red_active");
//		instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Red_Inactive] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipesignal_red_inactive");
//		instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Blue_Active] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipesignal_blue_active");
//		instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Blue_Inactive] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipesignal_blue_inactive");
//		instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Green_Active] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipesignal_green_active");
//		instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Green_Inactive] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipesignal_green_inactive");
//		instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Yellow_Active] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipesignal_yellow_active");
//		instance.itemIcons[IconItemConstants.Trigger_PipeSignal_Yellow_Inactive] = itemTextures.func_94245_a("buildcraft:triggers/trigger_pipesignal_yellow_inactive");
//		instance.itemIcons[IconItemConstants.Trigger_RedstoneInput_Active] = itemTextures.func_94245_a("buildcraft:triggers/trigger_redstoneinput_active");
//		instance.itemIcons[IconItemConstants.Trigger_RedstoneInput_Inactive] = itemTextures.func_94245_a("buildcraft:triggers/trigger_redstoneinput_inactive");
//		
//	}
}
