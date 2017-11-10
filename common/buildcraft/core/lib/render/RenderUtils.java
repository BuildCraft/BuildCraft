/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

public final class RenderUtils {

	/**
	 * Deactivate constructor
	 */
	private RenderUtils() {
	}

	public static void setGLColorFromInt(int color) {
		float red = (color >> 16 & 255) / 255.0F;
		float green = (color >> 8 & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;
		GL11.glColor4f(red, green, blue, 1.0F);
	}

	public static void drawBlockItem(RenderBlocks render, Tessellator tessellator, Block block, IIcon icon) {
		if (icon == null) {
			return;
		}
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1F, 0.0F);
		render.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		render.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.setNormal(0.0F, 0.0F, -1F);
		render.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		render.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		render.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		render.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
	}

	public static void drawBlockItem(RenderBlocks render, Tessellator tessellator, Block block, int decodedMeta) {
		tessellator.startDrawingQuads();
		IIcon icon = tryGetBlockIcon(block, 0, decodedMeta);
		if (icon != null) {
			tessellator.setNormal(0.0F, -1F, 0.0F);
			render.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, icon);
		}
		icon = tryGetBlockIcon(block, 1, decodedMeta);
		if (icon != null) {
			tessellator.setNormal(0.0F, 1F, 0.0F);
			render.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, icon);
		}
		icon = tryGetBlockIcon(block, 2, decodedMeta);
		if (icon != null) {
			tessellator.setNormal(0.0F, 0.0F, -1F);
			render.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, icon);
		}
		icon = tryGetBlockIcon(block, 3, decodedMeta);
		if (icon != null) {
			tessellator.setNormal(0.0F, 0.0F, 1F);
			render.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, icon);
		}
		icon = tryGetBlockIcon(block, 4, decodedMeta);
		if (icon != null) {
			tessellator.setNormal(-1F, 0.0F, 0.0F);
			render.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, icon);
		}
		icon = tryGetBlockIcon(block, 5, decodedMeta);
		if (icon != null) {
			tessellator.setNormal(1F, 0.0F, 0.0F);
			render.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, icon);
		}
		tessellator.draw();
	}

	public static IIcon tryGetBlockIcon(Block block, int side, int decodedMeta) {
		IIcon icon = null;

		try {
			icon = block.getIcon(side, decodedMeta);
		} catch (Throwable t) {
			try {
				icon = block.getBlockTextureFromSide(side);
			} catch (Throwable ignored) {
			}
		}

		return icon;
	}
}
