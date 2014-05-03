/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.core.DefaultProps;
import buildcraft.core.IInventoryRenderer;

public class RenderHopper extends TileEntitySpecialRenderer implements IInventoryRenderer {

	private static final ResourceLocation HOPPER_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/hopper.png");
	private static final ResourceLocation HOPPER_MIDDLE_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/hopper_middle.png");
	private ModelBase model = new ModelBase() {
	};
	private final ModelRenderer top;
	private final ModelFrustum middle;
	private final ModelRenderer bottom;

	public RenderHopper() {
		top = new ModelRenderer(model, 0, 0);
		top.addBox(-8F, 1F, -8F, 16, 7, 16);
		top.rotationPointX = 8F;
		top.rotationPointY = 8F;
		top.rotationPointZ = 8F;
		middle = new ModelFrustum(top, 32, 0, 0, 3, 0, 8, 8, 16, 16, 7, 1F / 16F);
		bottom = new ModelRenderer(model, 0, 23);
		bottom.addBox(-3F, -8F, -3F, 6, 3, 6);
		bottom.rotationPointX = 8F;
		bottom.rotationPointY = 8F;
		bottom.rotationPointZ = 8F;
		field_147501_a = TileEntityRendererDispatcher.instance;
	}

	@Override
	public void inventoryRender(double x, double y, double z, float f, float f1) {
		render(x, y, z);
	}

	@Override
	public void renderTileEntityAt(TileEntity var1, double x, double y, double z, float f) {
		render(x, y, z);
	}

	private void render(double x, double y, double z) {
		if (BuildCraftCore.render == RenderMode.NoDynamic) {
			return;
		}

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);

		GL11.glTranslated(x, y, z);
		bindTexture(HOPPER_TEXTURE);
		top.render((float) (1.0 / 16.0));
		bottom.render((float) (1.0 / 16.0));
		bindTexture(HOPPER_MIDDLE_TEXTURE);
		middle.render(Tessellator.instance, 1F / 16F);

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}
}
