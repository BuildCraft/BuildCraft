/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import net.minecraftforge.client.IItemRenderer;

import buildcraft.BuildCraftTransport;
import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemFacade.FacadeState;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.TransportConstants;

public class FacadeItemRenderer implements IItemRenderer {

	private long lastTime = 0L;

	private int renderState = 0;

	private IIcon tryGetBlockIcon(Block block, int side, int decodedMeta) {
		IIcon icon = RenderUtils.tryGetBlockIcon(block, side, decodedMeta);

		if (icon == null) {
			icon = PipeIconProvider.TYPE.TransparentFacade.getIcon();
		}

		return icon;
	}

	private void drawHollowCube(Tessellator tessellator, RenderBlocks render, Block block, int decodedMeta) {
		IIcon icon0 = tryGetBlockIcon(block, 0, decodedMeta);
		IIcon icon1 = tryGetBlockIcon(block, 1, decodedMeta);
		IIcon icon2 = tryGetBlockIcon(block, 2, decodedMeta);
		IIcon icon3 = tryGetBlockIcon(block, 3, decodedMeta);
		IIcon icon4 = tryGetBlockIcon(block, 4, decodedMeta);
		IIcon icon5 = tryGetBlockIcon(block, 5, decodedMeta);

		float width = 1 - TransportConstants.FACADE_THICKNESS;
		float cavity = (CoreConstants.PIPE_MAX_POS - CoreConstants.PIPE_MIN_POS) / 2F;
		double innerWidth = 1 - cavity;

		tessellator.startDrawingQuads();
		render.setRenderBounds(0F, 0F, width, 1F, 1F, 1F);

		//Outside
		tessellator.setNormal(0, -1, 0);
		render.renderFaceYNeg(block, 0, 0, 0, icon0);
		tessellator.setNormal(0, 1, 0);
		render.renderFaceYPos(block, 0, 0, 0, icon1);
		tessellator.setNormal(-1, 0, 0);
		render.renderFaceXNeg(block, 0, 0, 0, icon4);
		tessellator.setNormal(1, 0, 0);
		render.renderFaceXPos(block, 0, 0, 0, icon5);

		//Inside
		tessellator.setNormal(0, -1, 0);
		render.renderFaceYNeg(block, 0, innerWidth, 0, icon0);
		tessellator.setNormal(0, 1, 0);
		render.renderFaceYPos(block, 0, -innerWidth, 0, icon1);
		tessellator.setNormal(-1, 0, 0);
		render.renderFaceXNeg(block, innerWidth, 0, 0, icon4);
		tessellator.setNormal(1, 0, 0);
		render.renderFaceXPos(block, -innerWidth, 0, 0, icon5);

		//Hollow
		render.field_152631_f = true;
		render.setRenderBounds(0, 0, width, cavity, 1, 1);
		tessellator.setNormal(0, 0, -1);
		render.renderFaceZNeg(block, 0, 0, 0, icon2);
		tessellator.setNormal(0, 0, 1);
		render.renderFaceZPos(block, 0, 0, 0, icon3);
		render.setRenderBounds(innerWidth, 0, width, 1, 1, 1);
		tessellator.setNormal(0, 0, -1);
		render.renderFaceZNeg(block, 0, 0, 0, icon2);
		tessellator.setNormal(0, 0, 1);
		render.renderFaceZPos(block, 0, 0, 0, icon3);
		render.field_152631_f = false;

		render.setRenderBounds(cavity, 0, width, innerWidth, cavity, 1);
		tessellator.setNormal(0, 0, -1);
		render.renderFaceZNeg(block, 0, 0, 0, icon2);
		tessellator.setNormal(0, 0, 1);
		render.renderFaceZPos(block, 0, 0, 0, icon3);
		render.setRenderBounds(cavity, innerWidth, width, innerWidth, 1, 1);
		tessellator.setNormal(0, 0, -1);
		render.renderFaceZNeg(block, 0, 0, 0, icon2);
		tessellator.setNormal(0, 0, 1);
		render.renderFaceZPos(block, 0, 0, 0, icon3);

		tessellator.draw();
	}

	private void renderFacadeItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {
		if (lastTime < System.currentTimeMillis()) {
			// 12 = LCM(1, 2, 3, 4)
			renderState = (renderState + 1) % 12;
			lastTime = System.currentTimeMillis() + 1000L;
		}

		FacadeType type = ((IFacadeItem) item.getItem()).getFacadeType(item);
		FacadeState[] states = ItemFacade.getFacadeStates(item);
		FacadeState activeState = null;

		if (states.length > 0) {
			// TODO: Figure out why NEI causes states[] to be of length 0
			if (type == FacadeType.Basic) {
				activeState = states[0];
			} else if (type == FacadeType.Phased) {
				activeState = states[renderState % states.length];
			}
		}
		Block block = activeState != null ? activeState.block : null;
		int decodedMeta = activeState != null ? activeState.metadata : 0;
		boolean hollow = activeState != null ? activeState.hollow : false;

		Tessellator tessellator = Tessellator.instance;

		if (tryGetBlockIcon(block, 0, decodedMeta) == null) {
			return;
		}

		// Render Facade
		GL11.glPushMatrix();

		// Enable glBlending for transparency
		if (block != null) {
			if (block.getRenderBlockPass() > 0) {
				GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			}

			RenderUtils.setGLColorFromInt(block.getRenderColor(decodedMeta));
		}

		if (hollow) {
			GL11.glTranslatef(translateX, translateY, translateZ);
			drawHollowCube(tessellator, render, block, decodedMeta);
		} else {
			render.setRenderBounds(0F, 0F, 1 - TransportConstants.FACADE_THICKNESS, 1F, 1F, 1F);
			GL11.glTranslatef(translateX, translateY, translateZ);
			RenderUtils.drawBlockItem(render, tessellator, block, decodedMeta);
		}

		// Disable blending
		if (block != null && block.getRenderBlockPass() > 0) {
			GL11.glDisable(GL11.GL_BLEND);
		}

		GL11.glPopMatrix();

		RenderUtils.setGLColorFromInt(0xFFFFFF);

		// Render StructurePipe
		if (!hollow && block != null && (block.getMaterial() == null || block.getMaterial().isOpaque())) {
			block = BuildCraftTransport.genericPipeBlock;
			IIcon textureID = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal()); // Structure pipe

			render.setRenderBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS - 1F / 16F);
			GL11.glTranslatef(translateX, translateY, translateZ + 0.25F);

			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, -0F, 0.0F);
			render.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, textureID);
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			render.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, textureID);
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, -1F);
			render.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, textureID);
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, 1.0F);
			render.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, textureID);
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(-1F, 0.0F, 0.0F);
			render.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, textureID);
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(1.0F, 0.0F, 0.0F);
			render.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, textureID);
			tessellator.draw();
		}
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		switch (type) {
			case ENTITY:
				return true;
			case EQUIPPED:
				return true;
			case EQUIPPED_FIRST_PERSON:
				return true;
			case INVENTORY:
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {

		switch (type) {
			case ENTITY:
				GL11.glScalef(0.50F, 0.50F, 0.50F);
				renderFacadeItem((RenderBlocks) data[0], item, -0.6F, 0f, -0.6F);
				break;
			case EQUIPPED:
			case EQUIPPED_FIRST_PERSON:
				renderFacadeItem((RenderBlocks) data[0], item, 0F, 0F, 0f);
				break;
			case INVENTORY:
				GL11.glScalef(1.1F, 1.1F, 1.1F);
				renderFacadeItem((RenderBlocks) data[0], item, -0.3f, -0.35f, -0.7f);
				break;
			default:
		}
	}
}
