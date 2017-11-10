/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.core.lib.render.FluidRenderer;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.lib.utils.MathUtils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.factory.TileTank;

public class RenderTank extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

		TileTank tank = (TileTank) CoreProxy.proxy.getServerTile(tileentity);

		FluidStack liquid = tank.tank.getFluid();
		if (liquid == null || liquid.getFluid() == null || liquid.amount <= 0) {
			return;
		}

		// Workaround: The colorRenderCache from the server tile from getServerTile(...) does not get synced properly
		int color;
		if (tank.getWorldObj().isRemote) {
			color = tank.tank.colorRenderCache;
		} else {
			color = liquid.getFluid().getColor(liquid);
		}

		int[] displayList = FluidRenderer.getFluidDisplayLists(liquid, tileentity.getWorldObj(), false);
		if (displayList == null) {
			return;
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		bindTexture(TextureMap.locationBlocksTexture);
		RenderUtils.setGLColorFromInt(color);

		GL11.glTranslatef((float) x + 0.125F, (float) y + 0.5F, (float) z + 0.125F);
		GL11.glScalef(0.75F, 0.999F, 0.75F);
		GL11.glTranslatef(0, -0.5F, 0);

		int dl = (int) ((float) liquid.amount / (float) (tank.tank.getCapacity()) * (FluidRenderer.DISPLAY_STAGES - 1));
		GL11.glCallList(displayList[MathUtils.clamp(dl, 0, FluidRenderer.DISPLAY_STAGES - 1)]);

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
