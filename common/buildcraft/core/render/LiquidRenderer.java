/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.render;

import buildcraft.core.render.RenderEntityBlock.BlockInterface;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.liquids.LiquidStack;
import org.lwjgl.opengl.GL11;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class LiquidRenderer {

	private static Map<LiquidStack, int[]> flowingRenderCache = new HashMap<LiquidStack, int[]>();
	private static Map<LiquidStack, int[]> stillRenderCache = new HashMap<LiquidStack, int[]>();
	public static final int DISPLAY_STAGES = 100;
	private static final BlockInterface liquidBlock = new BlockInterface();

	public static Icon getLiquidTexture(LiquidStack liquid) {
		if (liquid == null || liquid.amount <= 0 || liquid.itemID <= 0) {
			return null;
		}

		return liquid.canonical().getRenderingIcon();
	}

	public static String setupFlowingLiquidTexture(LiquidStack liquid, Icon[] texArray) {
		if (liquid == null || liquid.amount <= 0 || liquid.itemID <= 0) {
			return null;
		}

		ItemStack stack = liquid.asItemStack();
		liquid = liquid.canonical();
		String texSheet = liquid.getTextureSheet();
		Icon top = liquid.getRenderingIcon();
		Icon side = top;
		if (stack.getItem() instanceof ItemBlock) {
			top = Block.blocksList[stack.itemID].getIcon(0, 0);
			side = Block.blocksList[stack.itemID].getIcon(2, 0);
			texSheet = "/terrain.png";
		}
		texArray[0] = top;
		texArray[1] = top;
		texArray[2] = side;
		texArray[3] = side;
		texArray[4] = side;
		texArray[5] = side;
		return texSheet;
	}

	public static int[] getLiquidDisplayLists(LiquidStack liquid, World world, boolean flowing) {
		if (liquid == null) {
			return null;
		}
		liquid = liquid.canonical();
		Map<LiquidStack, int[]> cache = flowing ? flowingRenderCache : stillRenderCache;
		int[] diplayLists = cache.get(liquid);
		if (diplayLists != null) {
			return diplayLists;
		}

		diplayLists = new int[DISPLAY_STAGES];

		if (liquid.itemID < Block.blocksList.length && Block.blocksList[liquid.itemID] != null) {
			liquidBlock.baseBlock = Block.blocksList[liquid.itemID];
			if (!flowing) {
				liquidBlock.texture = liquid.getRenderingIcon();
			}
		} else if (Item.itemsList[liquid.itemID] != null) {
			liquidBlock.baseBlock = Block.waterStill;
			liquidBlock.texture = liquid.getRenderingIcon();
		} else {
			return null;
		}

		cache.put(liquid.canonical(), diplayLists);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);
		ItemStack stack = liquid.asItemStack();
		int color = stack.getItem().getColorFromItemStack(stack, 0);
		float c1 = (float) (color >> 16 & 255) / 255.0F;
		float c2 = (float) (color >> 8 & 255) / 255.0F;
		float c3 = (float) (color & 255) / 255.0F;
		GL11.glColor4f(c1, c2, c3, 1);
		for (int s = 0; s < DISPLAY_STAGES; ++s) {
			diplayLists[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(diplayLists[s], 4864 /*GL_COMPILE*/);

			liquidBlock.minX = 0.01f;
			liquidBlock.minY = 0;
			liquidBlock.minZ = 0.01f;

			liquidBlock.maxX = 0.99f;
			liquidBlock.maxY = (float) s / (float) DISPLAY_STAGES;
			liquidBlock.maxZ = 0.99f;

			RenderEntityBlock.renderBlock(liquidBlock, world, 0, 0, 0, false, true);

			GL11.glEndList();
		}

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);

		return diplayLists;
	}
}
