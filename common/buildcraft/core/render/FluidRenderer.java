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
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class FluidRenderer {

	private static Map<FluidStack, int[]> flowingRenderCache = new HashMap<FluidStack, int[]>();
	private static Map<FluidStack, int[]> stillRenderCache = new HashMap<FluidStack, int[]>();
	public static final int DISPLAY_STAGES = 100;
	private static final BlockInterface liquidBlock = new BlockInterface();

	public static class FluidTextureException extends RuntimeException {

		private final FluidStack liquid;

		public FluidTextureException(FluidStack liquid) {
			super();
			this.liquid = liquid;
		}

		@Override
		public String getMessage() {
			String liquidName = FluidRegistry.getFluidName(liquid);
			if (liquidName == null) {
				liquidName = String.format("ID: %d Meta: %d", liquid.itemID, liquid.itemMeta);
			}
			return String.format("Fluid %s has no icon. Please contact the author of the mod the liquid came from.", liquidName);
		}
	}

	public static class FluidCanonException extends RuntimeException {

		private final FluidStack liquid;

		public FluidCanonException(FluidStack liquid) {
			super();
			this.liquid = liquid;
		}

		@Override
		public String getMessage() {
			String liquidName = FluidRegistry.getFluidName(liquid);
			if (liquidName == null) {
				liquidName = String.format("ID: %d Meta: %d", liquid.itemID, liquid.itemMeta);
			}
			return String.format("Fluid %s is not registered with the Fluid Dictionary. Please contact the author of the mod the liquid came from.", liquidName);
		}
	}

	public static Icon getFluidTexture(FluidStack liquid) {
		if (liquid == null || liquid.itemID <= 0) {
			return null;
		}
		FluidStack canon = liquid.canonical();
		if (canon == null) {
			throw new FluidCanonException(liquid);
		}
		Icon icon = canon.getRenderingIcon();
		if (icon == null) {
			throw new FluidTextureException(liquid);
		}
		return icon;
	}

	public static ResourceLocation getFluidSheet(FluidStack liquid) {
		if (liquid == null || liquid.itemID <= 0) {
			return "/terrain.png";
		}
		FluidStack canon = liquid.canonical();
		if (canon == null) {
			throw new FluidCanonException(liquid);
		}
		return canon.getTextureSheet();
	}

	public static int[] getFluidDisplayLists(FluidStack liquid, World world, boolean flowing) {
		if (liquid == null) {
			return null;
		}
		liquid = liquid.canonical();
		if(liquid == null){
			throw new FluidCanonException(liquid);
		}
		Map<FluidStack, int[]> cache = flowing ? flowingRenderCache : stillRenderCache;
		int[] diplayLists = cache.get(liquid);
		if (diplayLists != null) {
			return diplayLists;
		}

		diplayLists = new int[DISPLAY_STAGES];

		if (liquid.itemID < Block.blocksList.length && Block.blocksList[liquid.itemID] != null) {
			liquidBlock.baseBlock = Block.blocksList[liquid.itemID];
			if (!flowing) {
				liquidBlock.texture = getFluidTexture(liquid);
			}
		} else if (Item.itemsList[liquid.itemID] != null) {
			liquidBlock.baseBlock = Block.waterStill;
			liquidBlock.texture = getFluidTexture(liquid);
		} else {
			return null;
		}

		cache.put(liquid, diplayLists);

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
