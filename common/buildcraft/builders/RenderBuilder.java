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

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;

import buildcraft.builders.urbanism.RenderBoxProvider;
import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.render.RenderLaser;

public class RenderBuilder extends RenderBoxProvider {

	private final EntityItem dummyEntityItem = new EntityItem(null);
	private final RenderItem customRenderItem;

	public RenderBuilder () {
		customRenderItem = new RenderItem() {
			@Override
			public boolean shouldBob() {
				return false;
			}

			@Override
			public boolean shouldSpreadItems() {
				return false;
			}
		};
		customRenderItem.setRenderManager(RenderManager.instance);
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		super.renderTileEntityAt(tileentity, x, y, z, f);

		TileAbstractBuilder builder = (TileAbstractBuilder) tileentity;

		if (builder != null) {
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
		}

		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		GL11.glTranslated(-tileentity.xCoord, -tileentity.yCoord, -tileentity.zCoord);

		if (builder.getBuilders() != null) {
			for (BuildingItem i : builder.getBuilders()) {
				doRenderItem(i, 1.0F);
			}
		}

		GL11.glPopMatrix();
	}

	public void doRenderItem(BuildingItem i, float light) {
		if (i == null) {
			return;
		}

		i.displayUpdate();

		for (StackAtPosition s : i.getStacks()) {
			if (s.display) {
				float renderScale = 0.7f;
				GL11.glPushMatrix();
				GL11.glTranslatef((float) s.pos.x, (float) s.pos.y,
						(float) s.pos.z);
				GL11.glTranslatef(0, 0.25F, 0);
				GL11.glScalef(renderScale, renderScale, renderScale);
				dummyEntityItem.setEntityItemStack(s.stack);
				customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);

				GL11.glPopMatrix();
			}
		}
	}

}
