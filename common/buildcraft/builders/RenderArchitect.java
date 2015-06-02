/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.render.RenderBoxProvider;
import buildcraft.core.render.RenderLaser;

public class RenderArchitect extends RenderBoxProvider {

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f, int i) {
		super.renderTileEntityAt(tileentity, x, y, z, f, i);

		TileArchitect architect = (TileArchitect) tileentity;

		if (architect != null) {
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GL11.glTranslated(x, y, z);
			GL11.glTranslated(-tileentity.getPos().getX(), -tileentity.getPos().getY(), -tileentity.getPos().getZ());

			for (LaserData laser : architect.subLasers) {
				if (laser != null) {
					GL11.glPushMatrix();
					RenderLaser
							.doRenderLaserWave(tileentity.getWorld(),
									TileEntityRendererDispatcher.instance.renderEngine,
									laser, EntityLaser.LASER_BLUE);

					GL11.glPopMatrix();
				}
			}

			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
	}

}
