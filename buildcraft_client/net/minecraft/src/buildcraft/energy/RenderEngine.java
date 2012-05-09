/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftCore.RenderMode;
import net.minecraft.src.ModelBase;
import net.minecraft.src.ModelRenderer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.IInventoryRenderer;
import net.minecraft.src.buildcraft.energy.Engine.EnergyStage;
import net.minecraft.src.forge.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

public class RenderEngine extends TileEntitySpecialRenderer implements IInventoryRenderer {

	private ModelBase model = new ModelBase () {};

	private ModelRenderer box;
	private ModelRenderer trunk;
	private ModelRenderer movingBox;
	private ModelRenderer chamber;
	private String baseTexture;

	public RenderEngine () {

		//constructor:
		box = new ModelRenderer(model, 0, 0);
		box.addBox(-8F, -8F, -8F, 16, 4, 16);
		box.rotationPointX = 8;
		box.rotationPointY = 8;
		box.rotationPointZ = 8;

		trunk = new ModelRenderer(model, 0, 0);
		trunk.addBox(-4F, -4F, -4F, 8, 12, 8);
		trunk.rotationPointX = 8F;
		trunk.rotationPointY = 8F;
		trunk.rotationPointZ = 8F;

		movingBox = new ModelRenderer(model, 0, 0);
		movingBox.addBox(-8F, -4, -8F, 16, 4, 16);
		movingBox.rotationPointX = 8F;
		movingBox.rotationPointY = 8F;
		movingBox.rotationPointZ = 8F;

		chamber = new ModelRenderer(model, 0, 0);
		chamber.addBox(-5F, -4, -5F, 10, 2, 10);
		chamber.rotationPointX = 8F;
		chamber.rotationPointY = 8F;
		chamber.rotationPointZ = 8F;
	}

	public RenderEngine (String baseTexture) {
		this ();
		this.baseTexture = baseTexture;
	}

	@Override
	public void inventoryRender(double x, double y, double z,
			float f, float f1) {
		render(EnergyStage.Blue, 0.25F, Orientations.YPos,
				baseTexture, x, y, z);
	}


	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {

		Engine engine = ((IEngineProvider) tileentity).getEngine();

		if (engine != null)
			render(engine.getEnergyStage(), engine.progress, engine.orientation,
					engine.getTextureFile(), x, y, z);
	}

	private void render(EnergyStage energy, float progress,
			Orientations orientation, String baseTexture, double x, double y, double z) {

		if (BuildCraftCore.render == RenderMode.NoDynamic)
			return;

		GL11.glPushMatrix();
		GL11.glDisable(2896 /*GL_LIGHTING*/);

		GL11.glTranslatef((float)x, (float)y, (float)z);

		float step;

		if (progress > 0.5)
			step = 7.99F - (progress - 0.5F) * 2F * 7.99F;
		else
			step = progress * 2F * 7.99F;

		float [] angle = {0, 0, 0};
		float [] translate = {0, 0, 0};
		float translatefact = step / 16;

		switch (orientation) {
		case XPos:
			angle [2] = (float) -Math.PI / 2;
			translate [0] = 1;
			break;
		case XNeg:
			angle [2] = (float) Math.PI / 2;
			translate [0] = -1;
			break;
		case YPos:
			translate [1] = 1;
			break;
		case YNeg:
			angle [2] = (float) Math.PI;
			translate [1] = -1;
			break;
		case ZPos:
			angle [0] = (float) Math.PI / 2;
			translate [2] = 1;
			break;
		case ZNeg:
			angle [0] = (float) -Math.PI / 2;
			translate [2] = -1;
			break;
		}

		box.rotateAngleX = angle [0];
		box.rotateAngleY = angle [1];
		box.rotateAngleZ = angle [2];

		trunk.rotateAngleX = angle [0];
		trunk.rotateAngleY = angle [1];
		trunk.rotateAngleZ = angle [2];

		movingBox.rotateAngleX = angle [0];
		movingBox.rotateAngleY = angle [1];
		movingBox.rotateAngleZ = angle [2];

		chamber.rotateAngleX = angle [0];
		chamber.rotateAngleY = angle [1];
		chamber.rotateAngleZ = angle [2];

		float factor = (float) (1.0 / 16.0);

		MinecraftForgeClient.bindTexture(baseTexture);

		box.render(factor);

		GL11.glTranslatef(translate[0] * translatefact, translate[1]
				* translatefact, translate[2] * translatefact);
		movingBox.render(factor);
		GL11.glTranslatef(-translate[0] * translatefact, -translate[1]
				* translatefact, -translate[2] * translatefact);

		MinecraftForgeClient
				.bindTexture("/net/minecraft/src/buildcraft/energy/gui/chamber.png");

		float chamberf = 2F / 16F;

		for (int i = 0; i <= step + 2; i += 2) {
			chamber.render(factor);
			GL11.glTranslatef(translate[0] * chamberf, translate[1] * chamberf, translate[2] * chamberf);
		}

		for (int i = 0; i <= step + 2; i += 2)
			GL11.glTranslatef(-translate[0] * chamberf, -translate[1] * chamberf, -translate[2] * chamberf);

		String texture = "";

		switch (energy) {
		case Blue:
			texture = "/net/minecraft/src/buildcraft/energy/gui/trunk_blue.png";
			break;
		case Green:
			texture = "/net/minecraft/src/buildcraft/energy/gui/trunk_green.png";
			break;
		case Yellow:
			texture = "/net/minecraft/src/buildcraft/energy/gui/trunk_yellow.png";
			break;
		default:
			texture = "/net/minecraft/src/buildcraft/energy/gui/trunk_red.png";
			break;
		}

		MinecraftForgeClient.bindTexture(texture);

		trunk.render(factor);

		GL11.glEnable(2896 /*GL_LIGHTING*/);
		GL11.glPopMatrix();
	}
}
