/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import buildcraft.core.render.RenderLaser;
import buildcraft.silicon.TileLaser;

public class RenderLaserTile extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		TileLaser laser = (TileLaser) tileentity;

		if (laser != null) {
			GL11.glPushMatrix();
			GL11.glTranslated(x, y, z);
			GL11.glTranslated(-tileentity.xCoord, -tileentity.yCoord, -tileentity.zCoord);

			GL11.glPushMatrix();
			RenderLaser.doRenderLaser(TileEntityRendererDispatcher.instance.field_147553_e, laser.laser, laser.getTexture());
			GL11.glPopMatrix();

			GL11.glPopMatrix();
		}
	}
}
