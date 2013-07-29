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
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class FluidRenderer {

	private static final ResourceLocation BLOCK_TEXTURE = TextureMap.field_110575_b;
	private static Map<Fluid, int[]> flowingRenderCache = new HashMap<Fluid, int[]>();
	private static Map<Fluid, int[]> stillRenderCache = new HashMap<Fluid, int[]>();
	public static final int DISPLAY_STAGES = 100;
	private static final BlockInterface liquidBlock = new BlockInterface();

	public static class MissingFluidTextureException extends RuntimeException {

		private final Fluid fluid;

		public MissingFluidTextureException(Fluid fluid) {
			super();
			this.fluid = fluid;
		}

		@Override
		public String getMessage() {
			return String.format("Fluid %s has no icon. Please contact the author of the mod the fluid came from.", fluid.getName());
		}
	}

	public static Icon getFluidTexture(FluidStack fluidStack, boolean flowing) {
		if (fluidStack == null) {
			return null;
		}
		return getFluidTexture(fluidStack.getFluid(), flowing);
	}

	public static Icon getFluidTexture(Fluid fluid, boolean flowing) {
		if (fluid == null) {
			return null;
		}
		Icon icon = flowing ? fluid.getFlowingIcon() : fluid.getStillIcon();
		if (icon == null) {
			throw new MissingFluidTextureException(fluid);
		}
		return icon;
	}

	public static ResourceLocation getFluidSheet(FluidStack liquid) {
		if (liquid == null)
			return BLOCK_TEXTURE;
		return getFluidSheet(liquid.getFluid());
	}

	public static ResourceLocation getFluidSheet(Fluid liquid) {
		return BLOCK_TEXTURE;
	}

	public static int[] getFluidDisplayLists(FluidStack fluidStack, World world, boolean flowing) {
		if (fluidStack == null) {
			return null;
		}
		Fluid fluid = fluidStack.getFluid();
		if (fluid == null) {
			return null;
		}
		Map<Fluid, int[]> cache = flowing ? flowingRenderCache : stillRenderCache;
		int[] diplayLists = cache.get(fluid);
		if (diplayLists != null) {
			return diplayLists;
		}

		diplayLists = new int[DISPLAY_STAGES];

		if (fluid.getBlockID() > 0) {
			liquidBlock.baseBlock = Block.blocksList[fluid.getBlockID()];
			liquidBlock.texture = getFluidTexture(fluidStack, flowing);
		} else {
			liquidBlock.baseBlock = Block.waterStill;
			liquidBlock.texture = getFluidTexture(fluidStack, flowing);
		}

		cache.put(fluid, diplayLists);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);
		int color = fluid.getColor(fluidStack);
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

			RenderEntityBlock.INSTANCE.renderBlock(liquidBlock, world, 0, 0, 0, false, true);

			GL11.glEndList();
		}

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);

		return diplayLists;
	}
}
