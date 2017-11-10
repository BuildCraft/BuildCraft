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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import net.minecraftforge.client.IItemRenderer;

import buildcraft.BuildCraftTransport;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.render.FakeBlock;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.transport.PipeIconProvider;

public class PipeItemRenderer implements IItemRenderer {
	private static final float zFightOffset = 1 / 4096.0F;

	private void renderPipeItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT); //don't break other mods' guis when holding a pipe
		//force transparency
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);

		// GL11.glBindTexture(GL11.GL_TEXTURE_2D, 10);
		Tessellator tessellator = Tessellator.instance;

		Block block = FakeBlock.INSTANCE;
		IIcon icon = PipeIconProvider.TYPE.PipeStainedOverlay.getIcon();

		if (item.getItemDamage() >= 1) {
			GL11.glPushMatrix();

			int c = ColorUtils.getRGBColor(item.getItemDamage() - 1);
			GL11.glColor3ub((byte) (c >> 16), (byte) ((c >> 8) & 0xFF), (byte) (c & 0xFF));
			block.setBlockBounds(CoreConstants.PIPE_MIN_POS + zFightOffset, 0.0F + zFightOffset, CoreConstants.PIPE_MIN_POS + zFightOffset, CoreConstants.PIPE_MAX_POS - zFightOffset, 1.0F - zFightOffset, CoreConstants.PIPE_MAX_POS - zFightOffset);
			block.setBlockBoundsForItemRender();
			render.setRenderBoundsFromBlock(block);

			GL11.glTranslatef(translateX, translateY, translateZ);
			RenderUtils.drawBlockItem(render, tessellator, block, icon);
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			GL11.glColor3ub((byte) 255, (byte) 255, (byte) 255);
			GL11.glPopMatrix();
		}

		block = BuildCraftTransport.genericPipeBlock;
		icon = item.getItem().getIconFromDamage(0);

		if (icon == null) {
			icon = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
		}

		block.setBlockBounds(CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, 1.0F, CoreConstants.PIPE_MAX_POS);
		block.setBlockBoundsForItemRender();
		render.setRenderBoundsFromBlock(block);

		GL11.glTranslatef(translateX, translateY, translateZ);
		RenderUtils.drawBlockItem(render, tessellator, block, icon);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		GL11.glPopAttrib(); // nicely leave the rendering how it was
	}

	/**
	 * IItemRenderer implementation *
	 */
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
				renderPipeItem((RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
				break;
			case EQUIPPED:
				renderPipeItem((RenderBlocks) data[0], item, -0.4f, 0.50f, 0.35f);
				break;
			case EQUIPPED_FIRST_PERSON:
				renderPipeItem((RenderBlocks) data[0], item, -0.4f, 0.50f, 0.35f);
				break;
			case INVENTORY:
				renderPipeItem((RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
				break;
			default:
		}
	}
}
