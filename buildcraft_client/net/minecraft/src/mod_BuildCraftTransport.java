/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.transport.IPipeRenderState;
import net.minecraft.src.buildcraft.transport.PipeItemRenderer;
import net.minecraft.src.buildcraft.transport.PipeWorldRenderer;
import net.minecraft.src.buildcraft.transport.RenderPipe;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.forge.NetworkMod;

public class mod_BuildCraftTransport extends NetworkMod {

	public static mod_BuildCraftTransport instance;
	public final static PipeItemRenderer pipeItemRenderer = new PipeItemRenderer();
	public final static PipeWorldRenderer pipeWorldRenderer = new PipeWorldRenderer();

	public mod_BuildCraftTransport() {
		instance = this;
	}

	@Override
	public void modsLoaded() {
		super.modsLoaded();
		BuildCraftTransport.initialize();
		
		BuildCraftTransport.initializeModel(this);

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
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsStipes.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsVoid.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsVoid.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeItemsSandstone.shiftedIndex, pipeItemRenderer);
		MinecraftForgeClient.registerItemRenderer(BuildCraftTransport.pipeLiquidsSandstone.shiftedIndex, pipeItemRenderer);
	}

	public static void registerTilePipe(Class<? extends TileEntity> clas, String name) {
		ModLoader.registerTileEntity(clas, name, new RenderPipe());
	}

	@Override
	public String getVersion() {
		return DefaultProps.VERSION;
	}

	@Override
	public void load() {
		BuildCraftTransport.load();
	}

	@Override
	public boolean clientSideRequired() {
		return true;
	}

	@Override
	public boolean serverSideRequired() {
		return true;
	}
	
	@Override
	public boolean renderWorldBlock(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block, int modelID) {
		if (modelID != BuildCraftTransport.pipeModel) return true;
		
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		
		if (tile instanceof TileGenericPipe){
			TileGenericPipe pipeTile = (TileGenericPipe) tile;
			pipeWorldRenderer.renderPipe(renderer, world, block, ((IPipeRenderState)tile).getRenderState(), pipeTile.xCoord, pipeTile.yCoord, pipeTile.zCoord);
		}
//		if (tile != null && tile instanceof IPipeTile && ((IPipeTile)tile).isInitialized()) {
//			pipeWorldRenderer.renderPipe(renderer, world, tile, block);
//		}

		return true;
	}

	

}
