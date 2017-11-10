/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.builders.TileAbstractBuilder;

public class RenderBuilder extends RenderBoxProvider {
	private static final RenderBuildingItems renderItems = new RenderBuildingItems();

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		super.renderTileEntityAt(tileentity, x, y, z, f);

		TileAbstractBuilder builder = (TileAbstractBuilder) tileentity;

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glTranslated(x, y, z);
		GL11.glTranslated(-tileentity.xCoord, -tileentity.yCoord, -tileentity.zCoord);

		if (builder.getPathLaser() != null) {
			for (LaserData laser : builder.getPathLaser()) {
				if (laser != null) {
					GL11.glPushMatrix();
					RenderLaser
							.doRenderLaser(
									TileEntityRendererDispatcher.instance.field_147553_e,
									laser, EntityLaser.LASER_TEXTURES[4]);
					GL11.glPopMatrix();
				}
			}
		}

		//GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopAttrib();
		GL11.glPopMatrix();

		renderItems.render(tileentity, x, y, z);
	}

}
