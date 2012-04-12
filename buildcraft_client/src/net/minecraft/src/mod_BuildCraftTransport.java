/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.transport.ItemPipe;
import net.minecraft.src.buildcraft.transport.RenderPipe;
import net.minecraft.src.forge.IItemRenderer;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.forge.NetworkMod;

import org.lwjgl.opengl.GL11;

public class mod_BuildCraftTransport extends NetworkMod implements IItemRenderer {

	public static mod_BuildCraftTransport instance;

	public mod_BuildCraftTransport() {
		instance = this;
	}

	@Override
	public void modsLoaded () {
		super.modsLoaded();
		BuildCraftTransport.initialize();
		
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeItemsWood.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeItemsCobblestone.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeItemsStone.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeItemsIron.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeItemsGold.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeItemsDiamond.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeItemsObsidian.shiftedIndex, this);
		
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeLiquidsWood.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeLiquidsCobblestone.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeLiquidsStone.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeLiquidsIron.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipeLiquidsGold.shiftedIndex, this);
		
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipePowerWood.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipePowerStone.shiftedIndex, this);
		MinecraftForgeClient.registerItemRenderer(
				BuildCraftTransport.pipePowerGold.shiftedIndex, this);
				
	}
	
	public static void registerTilePipe (Class <? extends TileEntity> clas, String name) {
		ModLoader.registerTileEntity(clas, name, new RenderPipe());
	}
	
	@Override
	public String getVersion() {
		return DefaultProps.VERSION;
	}
	
	private void renderPipeItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {
		Tessellator tessellator = Tessellator.instance;

		Block block = BuildCraftTransport.genericPipeBlock;
		int textureID = ((ItemPipe) Item.itemsList [item.itemID]).getTextureIndex();
		if (textureID > 255) textureID -= 256;

		block.setBlockBounds(Utils.pipeMinPos, 0.0F, Utils.pipeMinPos,
				Utils.pipeMaxPos, 1.0F, Utils.pipeMaxPos);
		block.setBlockBoundsForItemRender();
		GL11.glTranslatef(translateX, translateY, translateZ);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1F, 0.0F);
		render.renderBottomFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		render.renderTopFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1F);
		render.renderEastFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		render.renderWestFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		render.renderNorthFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		render.renderSouthFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		
	}

	@Override
	public void load() {
		BuildCraftTransport.load();
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		switch (type) {
			case ENTITY: return true;
			case EQUIPPED: return true;
			case INVENTORY: return true;
		}
		return false;
	}
	
	@Override public boolean clientSideRequired() { return true; }
	@Override public boolean serverSideRequired() { return false; }

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		switch(type){
		case ENTITY:
			renderPipeItem((RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
			break;
		case EQUIPPED:
			renderPipeItem((RenderBlocks) data[0], item, -0.4f, 0.50f, 0.35f);
			break;
		case INVENTORY:
			renderPipeItem((RenderBlocks) data[0], item,  -0.5f, -0.5f, -0.5f );
			break;
		}
	}

}
