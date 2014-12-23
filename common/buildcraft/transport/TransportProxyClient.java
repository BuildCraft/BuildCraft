/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.core.utils.Utils;
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
		ClientRegistry.bindTileEntitySpecialRenderer(TileGenericPipe.class, rp);
	}

	private void registerPipeRenderer(Item item) {
		BCLog.logger.info("Registering model for " + item.getUnlocalizedName());
		final ModelResourceLocation loc = new ModelResourceLocation(Utils.getBlockName(BuildCraftTransport.genericPipeBlock), null);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, new ItemMeshDefinition() {
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				return loc;
			}
		});
	}

	@Override
	public void registerRenderers() {
		registerPipeRenderer(BuildCraftTransport.pipeItemsWood);
		registerPipeRenderer(BuildCraftTransport.pipeItemsCobblestone);
		registerPipeRenderer(BuildCraftTransport.pipeItemsStone);
		registerPipeRenderer(BuildCraftTransport.pipeItemsQuartz);
		registerPipeRenderer(BuildCraftTransport.pipeItemsIron);
		registerPipeRenderer(BuildCraftTransport.pipeItemsGold);
		registerPipeRenderer(BuildCraftTransport.pipeItemsDiamond);
		registerPipeRenderer(BuildCraftTransport.pipeItemsObsidian);
		registerPipeRenderer(BuildCraftTransport.pipeItemsEmerald);
		registerPipeRenderer(BuildCraftTransport.pipeItemsLapis);
		registerPipeRenderer(BuildCraftTransport.pipeItemsDaizuli);
		registerPipeRenderer(BuildCraftTransport.pipeItemsEmzuli);
		registerPipeRenderer(BuildCraftTransport.pipeItemsStripes);
        registerPipeRenderer(BuildCraftTransport.pipeItemsClay);

		registerPipeRenderer(BuildCraftTransport.pipeFluidsWood);
		registerPipeRenderer(BuildCraftTransport.pipeFluidsCobblestone);
		registerPipeRenderer(BuildCraftTransport.pipeFluidsStone);
		registerPipeRenderer(BuildCraftTransport.pipeFluidsQuartz);
		registerPipeRenderer(BuildCraftTransport.pipeFluidsIron);
		registerPipeRenderer(BuildCraftTransport.pipeFluidsGold);
		registerPipeRenderer(BuildCraftTransport.pipeFluidsDiamond);
		registerPipeRenderer(BuildCraftTransport.pipeFluidsEmerald);

		registerPipeRenderer(BuildCraftTransport.pipePowerWood);
		registerPipeRenderer(BuildCraftTransport.pipePowerCobblestone);
		registerPipeRenderer(BuildCraftTransport.pipePowerStone);
		registerPipeRenderer(BuildCraftTransport.pipePowerQuartz);
		registerPipeRenderer(BuildCraftTransport.pipePowerIron);
		registerPipeRenderer(BuildCraftTransport.pipePowerGold);
		registerPipeRenderer(BuildCraftTransport.pipePowerDiamond);
		registerPipeRenderer(BuildCraftTransport.pipePowerEmerald);
		registerPipeRenderer(BuildCraftTransport.pipeStructureCobblestone);

		registerPipeRenderer(BuildCraftTransport.pipeItemsVoid);
		registerPipeRenderer(BuildCraftTransport.pipeFluidsVoid);
		registerPipeRenderer(BuildCraftTransport.pipeItemsSandstone);
		registerPipeRenderer(BuildCraftTransport.pipeFluidsSandstone);
        registerPipeRenderer(BuildCraftTransport.pipePowerSandstone);

		/*registerPipeRenderer(BuildCraftTransport.facadeItem, facadeItemRenderer);
		registerPipeRenderer(BuildCraftTransport.plugItem, plugItemRenderer);
		registerPipeRenderer(BuildCraftTransport.robotStationItem, robotStationItemRenderer);
		registerPipeRenderer(BuildCraftTransport.pipeGate, gateItemRenderer);*/

		// TODO: PIPEWORLDRENDERER
	}

	@Override
	public void setIconProviderFromPipe(ItemPipe item, Pipe<?> dummyPipe) {
		//item.setPipesIcons(dummyPipe.getIconProvider());
	}
}
