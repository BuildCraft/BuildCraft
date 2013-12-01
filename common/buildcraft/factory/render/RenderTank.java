/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.render;

import buildcraft.core.render.FluidRenderer;
import buildcraft.factory.TileTank;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class RenderTank extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

		TileTank tank = ((TileTank) tileentity);

		FluidStack liquid = tank.tank.getFluid();
		int color = tank.tank.colorRenderCache;
		if (liquid == null || liquid.amount <= 0) {
			return;
		}

		int[] displayList = FluidRenderer.getFluidDisplayLists(liquid, tileentity.worldObj, false);
		if (displayList == null) {
			return;
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		bindTexture(FluidRenderer.getFluidSheet(liquid));
		float red = (float) (color >> 16 & 255) / 255.0F;
		float green = (float) (color >> 8 & 255) / 255.0F;
		float blue = (float) (color & 255) / 255.0F;
		GL11.glColor4f(red, green, blue, 1.0F);
                
		GL11.glTranslatef((float) x + 0.125F, (float) y + 0.5F, (float) z + 0.125F);
		GL11.glScalef(0.75F, 0.999F, 0.75F);
		GL11.glTranslatef(0, -0.5F, 0);

		GL11.glCallList(displayList[(int) ((float) liquid.amount / (float) (tank.tank.getCapacity()) * (FluidRenderer.DISPLAY_STAGES - 1))]);

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
