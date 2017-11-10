/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui.widgets;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.core.lib.fluids.Tank;
import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.core.lib.render.FluidRenderer;

public class FluidGaugeWidget extends Widget {

	public final Tank tank;

	public FluidGaugeWidget(Tank tank, int x, int y, int u, int v, int w, int h) {
		super(x, y, u, v, w, h);
		this.tank = tank;
	}

	@Override
	public ToolTip getToolTip() {
		return tank.getToolTip();
	}

	@Override
	public void draw(GuiBuildCraft gui, int guiX, int guiY, int mouseX, int mouseY) {
		if (tank == null) {
			return;
		}
		FluidStack fluidStack = tank.getFluid();
		if (fluidStack == null || fluidStack.amount <= 0 || fluidStack.getFluid() == null) {
			return;
		}

		IIcon liquidIcon = FluidRenderer.getFluidTexture(fluidStack, false);

		if (liquidIcon == null) {
			return;
		}

		float scale = Math.min(fluidStack.amount, tank.getCapacity()) / (float) tank.getCapacity();

		gui.bindTexture(TextureMap.locationBlocksTexture);

		for (int col = 0; col < w / 16; col++) {
			for (int row = 0; row <= h / 16; row++) {
				gui.drawTexturedModelRectFromIcon(guiX + x + col * 16, guiY + y + row * 16 - 1, liquidIcon, 16, 16);
			}
		}

		gui.bindTexture(gui.texture);

		gui.drawTexturedModalRect(guiX + x, guiY + y - 1, x, y - 1, w, h - (int) Math.floor(h * scale) + 1);
		gui.drawTexturedModalRect(guiX + x, guiY + y, u, v, w, h);
	}

}
