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

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.core.EntityLaser;
import buildcraft.core.render.RenderBoxProvider;
import buildcraft.core.render.RenderBuildingItems;
import buildcraft.core.render.RenderLaser;

public class RenderConstructionMarker extends RenderBoxProvider {

	private final RenderBuildingItems renderItems = new RenderBuildingItems();

	private final EntityItem dummyEntityItem = new EntityItem(null);
	private final RenderItem customRenderItem;

	private ModelBase model = new ModelBase() {
	};
	private ModelRenderer box;

	public RenderConstructionMarker() {
		box = new ModelRenderer(model, 0, 1);
		box.addBox(-8F, -8F, -8F, 16, 4, 16);
		box.rotationPointX = 8;
		box.rotationPointY = 8;
		box.rotationPointZ = 8;

		customRenderItem = Minecraft.getMinecraft().getRenderItem();
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f, int i) {
		super.renderTileEntityAt(tileentity, x, y, z, f, i);

		TileConstructionMarker marker = (TileConstructionMarker) tileentity;

		if (marker != null) {
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GL11.glTranslated(x, y, z);
			GL11.glTranslated(-tileentity.getPos().getX(), -tileentity.getPos().getY(), -tileentity.getPos().getZ());

			if (marker.laser != null) {
				GL11.glPushMatrix();
				RenderLaser
						.doRenderLaser(tileentity.getWorld(),
								TileEntityRendererDispatcher.instance.renderEngine,
								marker.laser, EntityLaser.LASER_BLUE);
				GL11.glPopMatrix();
			}

			if (marker.itemBlueprint != null) {
				doRenderItem(marker.itemBlueprint,
					marker.getPos().getX() + 0.5F,
					marker.getPos().getY() + 0.2F,
					marker.getPos().getZ() + 0.5F);
			}

			//GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopAttrib();
			GL11.glPopMatrix();

			renderItems.render(tileentity, x, y, z);
		}
	}

	public void doRenderItem(ItemStack stack, double x, double y, double z) {
		if (stack == null) {
			return;
		}

		float renderScale = 1.5f;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GL11.glTranslatef(0, 0.25F, 0);
		GL11.glScalef(renderScale, renderScale, renderScale);
		dummyEntityItem.setEntityItemStack(stack);
		//customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);

		GL11.glPopMatrix();
	}
}
