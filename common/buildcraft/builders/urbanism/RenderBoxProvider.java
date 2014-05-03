/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.Box;
import buildcraft.core.DefaultProps;
import buildcraft.core.IBoxProvider;
import buildcraft.core.IBoxesProvider;
import buildcraft.core.render.RenderBox;

public class RenderBoxProvider extends TileEntitySpecialRenderer {

	private static final ResourceLocation LASER_RED = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_1.png");
	private static final ResourceLocation LASER_YELLOW = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_2.png");
	private static final ResourceLocation LASER_GREEN = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_3.png");
	private static final ResourceLocation LASER_BLUE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_4.png");
	private static final ResourceLocation STRIPES = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/stripes.png");

	public RenderBoxProvider() {
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glPushMatrix();
		GL11.glTranslated(-tileentity.xCoord, -tileentity.yCoord, -tileentity.zCoord);
		GL11.glTranslated(x, y, z);

		if (tileentity instanceof IBoxesProvider) {
			for (Box b : ((IBoxesProvider) tileentity).getBoxes()) {
				if (b.isVisible) {
					RenderBox.doRender(
						TileEntityRendererDispatcher.instance.field_147553_e,
						getTexture(b.kind), b);
				}
			}
		} else if (tileentity instanceof IBoxProvider) {
			Box b = ((IBoxProvider) tileentity).getBox();

			if (b.isVisible) {
				RenderBox.doRender(
					TileEntityRendererDispatcher.instance.field_147553_e,
					getTexture(b.kind), b);
			}
		}

		GL11.glPopMatrix();

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	private ResourceLocation getTexture(Box.Kind kind) {
		switch (kind) {
		case LASER_RED:
			return LASER_RED;
		case LASER_YELLOW:
			return LASER_YELLOW;
		case LASER_GREEN:
			return LASER_GREEN;
		case LASER_BLUE:
			return LASER_BLUE;
		case STRIPES:
			return STRIPES;
		}

		return null;
	}
}
