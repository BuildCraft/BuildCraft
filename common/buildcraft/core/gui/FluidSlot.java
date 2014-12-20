/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.Fluid;

import buildcraft.core.render.FluidRenderer;
import buildcraft.core.render.RenderUtils;

/**
 * For the refinery, a kind of phantom slot for fluid.
 */
//TODO Get this class working well (Now it's just here to let the refinery compil)
public class FluidSlot extends AdvancedSlot {

	public Fluid fluid;
	public int colorRenderCache;

	public FluidSlot(GuiAdvancedInterface gui, int x, int y) {
		super(gui, x, y);
	}

	@Override
	public void drawSprite(int cornerX, int cornerY) {
		if (fluid != null) {
			RenderUtils.setGLColorFromInt(colorRenderCache);
		}
		super.drawSprite(cornerX, cornerY);
	}

	/*@Override
	public IIcon getIcon() {
		return FluidRenderer.getFluidTexture(fluid, false);
	}

	@Override
	public ResourceLocation getTexture() {
		return FluidRenderer.getFluidSheet(fluid);
	}*/
}